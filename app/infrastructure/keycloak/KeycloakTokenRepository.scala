package infrastructure.keycloak

import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.api.mvc.MultipartFormData
import services.{AuthTokenRepository, SettingsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakTokenRepository @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends AuthTokenRepository {

  override def logout(clientId: String, refreshToken: String): Future[Unit] = {
    val host = settingsService.keycloakConfig.serverUrl
    val dataParts = Map("refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(s"$host/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/logout")
      .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .post(dataParts)
      .map(_ => ())
  }

  override def getToken(username: String, password: String, clientId: String): Future[JsValue] = {
    val host = settingsService.keycloakConfig.serverUrl
    val dataParts = Map(
      "grant_type" -> Seq("password"),
      "username" -> Seq(username),
      "password" -> Seq(password),
      "client_id" -> Seq(clientId),
    )
    wsClient
      .url(s"$host/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map(_.json)
  }
}
