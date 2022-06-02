package services

import com.google.inject.ImplementedBy
import infrastructure.keycloak.KeycloakTokenRepository
import play.api.libs.json.JsValue

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakTokenRepository])
trait AuthTokenRepository {
  def getToken(username: String, password: String): Future[JsValue]
}
