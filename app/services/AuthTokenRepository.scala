package services

import com.google.inject.ImplementedBy
import domain.errors
import infrastructure.keycloak.KeycloakTokenRepository
import play.api.libs.json.JsValue

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakTokenRepository])
trait AuthTokenRepository {
  def refreshAccessToken(realm: String, clientId: String, grantType: String, refreshToken: String): Future[JsValue]
  def getTokenWithCredentials(username: String, password: String, clientId: String): Future[JsValue]
  def getTokenWithAuthCode(provider: String, authCode: String, clientId: String): Future[Either[errors.Error, JsValue]]
  def logout(clientId: String, refreshToken: String): Future[Unit]
}
