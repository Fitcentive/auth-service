package infrastructure.keycloak

import play.api.libs.json.JsValue
import play.api.libs.ws.{EmptyBody, WSClient}
import play.api.mvc.MultipartFormData
import services.{AuthTokenRepository, SettingsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakTokenRepository @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends AuthTokenRepository {

  private val authServerHost: String = settingsService.keycloakConfig.serverUrl

  override def refreshAccessToken(clientId: String, grantType: String, refreshToken: String): Future[JsValue] = {
    val dataParts =
      Map("grant_type" -> Seq(grantType), "refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(s"$authServerHost/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map(_.json)
  }

  override def logout(clientId: String, refreshToken: String): Future[Unit] = {
    val dataParts = Map("refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(s"$authServerHost/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/logout")
      .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .post(dataParts)
      .map(_ => ())
  }

  override def getToken(username: String, password: String, clientId: String): Future[JsValue] = {
    val dataParts = Map(
      "grant_type" -> Seq("password"),
      "username" -> Seq(username),
      "password" -> Seq(password),
      "client_id" -> Seq(clientId),
    )
    wsClient
      .url(s"$authServerHost/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map(_.json)
  }
}
