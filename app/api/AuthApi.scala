package api

import domain.User
import play.api.libs.json.JsValue
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}
import services.{AuthAdminRepository, AuthTokenRepository, SettingsService}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthApi @Inject() (
  authAdminRepo: AuthAdminRepository,
  settingsService: SettingsService,
  authTokenRepository: AuthTokenRepository
)(implicit ec: ExecutionContext) {

  def createNewUser(user: User): Future[Unit] = authAdminRepo.createUser(user)

  def resetPassword(userName: String, password: String): Future[Unit] = authAdminRepo.resetPassword(userName, password)

  def oidcLoginWithRedirect(provider: String, rawRequest: Request[AnyContent]): Future[Result] = {
    val serverUrl = settingsService.keycloakConfig.serverUrl
    for {
      loginUrl <- provider match {
        case "google" => Future.successful(s"$serverUrl/${settingsService.keycloakConfig.googleOidcLoginUrl}")
        case _        => Future.failed(new Exception("Unrecognized OIDC provider"))
      }
      result <- Future.successful(Redirect(loginUrl, rawRequest.queryString))
    } yield result
  }

  def generateToken(userName: String, password: String, clientId: String): Future[JsValue] =
    authTokenRepository.getTokenWithCredentials(userName, password, clientId)

  def generateToken(authCode: String, clientId: String): Future[JsValue] =
    authTokenRepository.getTokenWithAuthCode(authCode, clientId)

  def refreshAccessToken(realm: String, clientId: String, grantType: String, refreshToken: String): Future[JsValue] =
    authTokenRepository.refreshAccessToken(realm, clientId, grantType, refreshToken)

  def logout(clientId: String, refreshToken: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken)

}
