package infrastructure.keycloak

import akka.stream.scaladsl.Source
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import play.api.mvc.{Headers, MultipartFormData}
import play.api.mvc.MultipartFormData.DataPart
import services.{AuthTokenRepository, SettingsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakTokenRepository @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends AuthTokenRepository {

  override def getToken(username: String, password: String): Future[JsValue] = {
    val host = settingsService.keycloakConfig.getString("server-url")
    val dataParts = Map(
      "grant_type" -> Seq("password"),
      "username" -> Seq(username),
      "password" -> Seq(password),
      "client_id" -> Seq("webapp"),
    )
    val multiPartFormData = MultipartFormData(dataParts, Seq.empty, Seq.empty)
    println("Trying to get token now")
    wsClient
      .url(s"$host/realms/${KeycloakAdminRepository.nativeAuthRealm}/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map { response =>
        println(s"Response has been received with status: ${response.status}")
        println(response.body)
        println("-------------------------")
        println(response.json)
        response.json
      }
  }
}
