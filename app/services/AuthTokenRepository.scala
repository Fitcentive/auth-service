package services

import com.google.inject.ImplementedBy
import infrastructure.keycloak.KeycloakTokenRepository
import play.api.libs.json.JsValue

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakTokenRepository])
trait AuthTokenRepository {
  def refreshAccessToken(clientId: String, grantType: String, refreshToken: String): Future[JsValue]
  def getToken(username: String, password: String, clientId: String): Future[JsValue]
  def logout(clientId: String, refreshToken: String): Future[Unit]
}
