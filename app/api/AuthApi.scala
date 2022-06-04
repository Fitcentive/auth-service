package api

import domain.User
import infrastructure.utils.AuthProviderOps
import play.api.libs.json.JsValue
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}
import services.{AuthAdminRepository, AuthTokenRepository, SettingsService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthApi @Inject() (
  authAdminRepo: AuthAdminRepository,
  authTokenRepository: AuthTokenRepository,
  authProviderOps: AuthProviderOps,
)(implicit ec: ExecutionContext) {

  def createNewUser(user: User): Future[Unit] = authAdminRepo.createUser(user)

  def resetPassword(userName: String, password: String): Future[Unit] = authAdminRepo.resetPassword(userName, password)

  def oidcLoginWithRedirect(provider: String, rawRequest: Request[AnyContent]): Future[Result] = {
    for {
      loginUrl <- authProviderOps.providerToLoginUrl(provider)
      result <- Future.successful(Redirect(loginUrl, rawRequest.queryString))
    } yield result
  }

  def generateTokenFromCredentials(userName: String, password: String, clientId: String): Future[JsValue] =
    authTokenRepository.getTokenWithCredentials(userName, password, clientId)

  def generateTokenFromAuthCode(provider: String, authCode: String, clientId: String): Future[JsValue] =
    authTokenRepository.getTokenWithAuthCode(provider, authCode, clientId)

  def refreshAccessToken(realm: String, clientId: String, grantType: String, refreshToken: String): Future[JsValue] =
    authTokenRepository.refreshAccessToken(realm, clientId, grantType, refreshToken)

  def logout(clientId: String, refreshToken: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken)

}
