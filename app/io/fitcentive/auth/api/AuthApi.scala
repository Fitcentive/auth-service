package io.fitcentive.auth.api

import cats.data.EitherT
import io.fitcentive.auth.domain.errors.OidcTokenValidationError
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.domain.{AuthorizedUserWithoutId, BasicAuthKeycloakUser, OidcTokenResponse}
import io.fitcentive.auth.repositories.AuthAdminRepository
import io.fitcentive.auth.services.{AuthTokenService, UserService}
import io.fitcentive.sdk.domain.TokenValidationService
import io.fitcentive.sdk.error.DomainError
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
      userOpt <- EitherT.right[DomainError](userService.getUserByEmail(authorizedUser.email))
      providerRealm <- EitherT(Future.successful(authProviderOps.providerToRealm(Some(provider))))
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
      _ <- authAdminRepo.addAttributesToSsoKeycloakUser(ssoProviderRealm, user.email, newAppUser.id)
      newToken <- authTokenRepository.refreshAccessToken(ssoProviderRealm, clientId, refreshToken)
    } yield newToken
  }

  def refreshAccessToken(realm: String, clientId: String, refreshToken: String): Future[JsValue] =
    authTokenRepository.refreshAccessToken(realm, clientId, refreshToken)

  def logout(clientId: String, refreshToken: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken)

}
