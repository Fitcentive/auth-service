package services

import com.google.inject.ImplementedBy
import infrastructure.keycloak.KeycloakTokenRepository
import play.api.libs.json.JsValue
import play.api.mvc.{AnyContent, Request}

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakTokenRepository])
trait AuthTokenRepository {
  def refreshAccessToken(realm: String, clientId: String, grantType: String, refreshToken: String): Future[JsValue]
  def getTokenWithCredentials(username: String, password: String, clientId: String): Future[JsValue]
  def getTokenWithAuthCode(authCode: String, clientId: String): Future[JsValue]
  def oidcLogin(rawRequest: Request[AnyContent]): Future[Unit]
  def logout(clientId: String, refreshToken: String): Future[Unit]
}
