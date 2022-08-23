package io.fitcentive.auth.api

import cats.data.EitherT
import io.fitcentive.auth.domain.errors.OidcTokenValidationError
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.domain.{BasicAuthKeycloakUser, OidcTokenResponse, UpdateKeycloakUserProfile, User}
import io.fitcentive.auth.repositories.AuthAdminRepository
import io.fitcentive.auth.services.{AuthTokenService, UserService}
import io.fitcentive.sdk.domain.TokenValidationService
import io.fitcentive.sdk.error.{DomainError, EntityConflictError, EntityNotFoundError}
import io.fitcentive.sdk.play.domain.AuthorizedUserWithoutId
import play.api.libs.json.JsValue
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import scala.util.chaining.scalaUtilChainingOps

@Singleton
class AuthApi @Inject() (
  authAdminRepo: AuthAdminRepository,
  authTokenRepository: AuthTokenService,
  authProviderOps: AuthProviderOps,
  tokenValidationService: TokenValidationService,
  userService: UserService,
)(implicit ec: ExecutionContext) {

  def updateKeycloakUserProfile(user: UpdateKeycloakUserProfile): Future[Either[DomainError, Unit]] = {
    (for {
      _ <- EitherT[Future, DomainError, Unit](authAdminRepo.checkIfUserExists(user.authProvider, user.email).map {
        case true  => Right()
        case false => Left(EntityNotFoundError("No user found!"))
      })
      _ <- EitherT.right[DomainError](
        authAdminRepo.updateUserProfile(user.authProvider, user.email, user.firstName, user.lastName)
      )
    } yield ()).value
  }

  def createNewKeycloakUser(user: BasicAuthKeycloakUser): Future[Unit] =
    authAdminRepo.createUserWithBasicAuth(user)

  def resetPassword(email: String, password: String): Future[Unit] = authAdminRepo.resetPassword(email, password)

  def oidcLoginWithRedirect(provider: String, rawRequest: Request[AnyContent]): Future[Either[DomainError, Result]] = {
    (for {
      loginUrl <-
        EitherT[Future, DomainError, String](Future.successful(authProviderOps.providerToExternalLoginUrl(provider)))
      result <- EitherT.right[DomainError](Future.successful(Redirect(loginUrl, rawRequest.queryString)))
    } yield result).value
  }

  def createNewDomainSsoUser(
    newSsoUser: AuthorizedUserWithoutId,
    providerRealm: String
  ): Future[Either[DomainError, Unit]] = {
    (for {
      _ <- EitherT[Future, DomainError, Unit](
        userService
          .getUserByEmailAndRealm(newSsoUser.email, providerRealm)
          .map(_.map(_ => Left(EntityConflictError("User already exists!"))).getOrElse(Right()))
      )
      newAppUser <- EitherT.right[DomainError](userService.createSsoUser(newSsoUser.email, providerRealm))
      _ <- EitherT.right[DomainError](
        userService.updateUserProfile(newAppUser.id, newSsoUser.firstName, newSsoUser.lastName)
      )
      _ <- EitherT.right[DomainError](
        authAdminRepo.addAttributesToSsoKeycloakUser(providerRealm, newSsoUser.email, newAppUser.id)
      )
    } yield ()).value
  }

  def generateAccessTokenAndCreateUserIfNeeded(
    provider: String,
    authCode: String,
    clientId: String
  ): Future[Either[DomainError, JsValue]] = {
    (for {
      authTokens <-
        EitherT[Future, DomainError, JsValue](authTokenRepository.getTokenWithAuthCode(provider, authCode, clientId))
      parsedTokens <- EitherT[Future, DomainError, OidcTokenResponse](
        Future
          .fromTry(Try(authTokens.as[OidcTokenResponse]))
          .map(Right.apply)
          .recover(ex => Left(OidcTokenValidationError(ex.getMessage)))
      )
      authorizedUser <- EitherT[Future, DomainError, AuthorizedUserWithoutId](
        tokenValidationService.validateJwt[AuthorizedUserWithoutId](parsedTokens.access_token).pipe(Future.successful)
      )
      providerRealm <- EitherT(Future.successful(authProviderOps.providerToRealm(Some(provider))))
      userOpt <- EitherT.right[DomainError](userService.getUserByEmailAndRealm(authorizedUser.email, providerRealm))
      result <- EitherT.right[DomainError] {
        if (userOpt.isDefined) Future.successful(authTokens)
        else createNewAppUserAndRefreshTokens(authorizedUser, providerRealm, clientId, parsedTokens.refresh_token)
      }
    } yield result).value
  }

  private def createNewAppUserAndRefreshTokens(
    user: AuthorizedUserWithoutId,
    ssoProviderRealm: String,
    clientId: String,
    refreshToken: String
  ): Future[JsValue] = {
    for {
      newAppUser <- userService.createSsoUser(user.email, ssoProviderRealm)
      _ <- userService.updateUserProfile(newAppUser.id, user.firstName, user.lastName)
      _ <- authAdminRepo.addAttributesToSsoKeycloakUser(ssoProviderRealm, user.email, newAppUser.id)
      newToken <- authTokenRepository.refreshAccessToken(ssoProviderRealm, clientId, refreshToken)
    } yield newToken
  }

  def refreshAccessToken(realm: String, clientId: String, refreshToken: String): Future[JsValue] =
    authTokenRepository.refreshAccessToken(realm, clientId, refreshToken)

  def logout(clientId: String, refreshToken: String, providerRealm: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken, providerRealm)

}
