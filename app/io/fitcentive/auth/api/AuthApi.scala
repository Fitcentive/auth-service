package io.fitcentive.auth.api

import cats.data.EitherT
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.domain.User
import io.fitcentive.auth.repositories.{AuthAdminRepository, AuthTokenRepository}
import io.fitcentive.sdk.error.DomainError
import play.api.libs.json.JsValue
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthApi @Inject() (
  authAdminRepo: AuthAdminRepository,
  authTokenRepository: AuthTokenRepository,
  authProviderOps: AuthProviderOps,
)(implicit ec: ExecutionContext) {

  def createNewUser(user: User): Future[Either[DomainError, Unit]] = authAdminRepo.createUser(user)

  def resetPassword(userName: String, password: String): Future[Unit] = authAdminRepo.resetPassword(userName, password)

  def oidcLoginWithRedirect(provider: String, rawRequest: Request[AnyContent]): Future[Either[DomainError, Result]] = {
    (for {
      loginUrl <- EitherT[Future, DomainError, String](Future.successful(authProviderOps.providerToLoginUrl(provider)))
      result <- EitherT.right[DomainError](Future.successful(Redirect(loginUrl, rawRequest.queryString)))
    } yield result).value
  }

  def generateTokenFromCredentials(userName: String, password: String, clientId: String): Future[JsValue] =
    authTokenRepository.getTokenWithCredentials(userName, password, clientId)

  def generateTokenFromAuthCode(
    provider: String,
    authCode: String,
    clientId: String
  ): Future[Either[DomainError, JsValue]] =
    authTokenRepository.getTokenWithAuthCode(provider, authCode, clientId)

  def refreshAccessToken(realm: String, clientId: String, grantType: String, refreshToken: String): Future[JsValue] =
    authTokenRepository.refreshAccessToken(realm, clientId, grantType, refreshToken)

  def logout(clientId: String, refreshToken: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken)

}
