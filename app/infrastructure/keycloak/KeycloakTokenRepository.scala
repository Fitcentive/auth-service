package infrastructure.keycloak

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import play.api.http.Status
import play.api.libs.json.JsValue
import play.api.libs.ws.{EmptyBody, WSClient}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, MultipartFormData, Request}
import services.{AuthTokenRepository, SettingsService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakTokenRepository @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: ExecutionContext
) extends AuthTokenRepository {

  private val authServerHost: String = settingsService.keycloakConfig.serverUrl

  override def oidcLogin(rawRequest: Request[AnyContent]): Future[Unit] = {
    wsClient
      .url(s"$authServerHost/realms/GoogleAuth/protocol/openid-connect/auth")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .withQueryStringParameters(rawRequest.queryString.toList.map(tuple => (tuple._1, tuple._2.head)): _*)
      .get()
      .flatMap { resp =>
        resp.status match {
//          case Status.SEE_OTHER => Future.unit
          case status =>
            println(s"Received status: ${status}")
            println(resp.body)
            Future.unit
//            Future.failed(new Exception("Bad status"))
        }
      }
  }

  override def refreshAccessToken(
    realm: String,
    clientId: String,
    grantType: String,
    refreshToken: String
  ): Future[JsValue] = {
    val dataParts =
      Map("grant_type" -> Seq(grantType), "refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(s"$authServerHost/realms/$realm/protocol/openid-connect/token")
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

  override def getTokenWithCredentials(username: String, password: String, clientId: String): Future[JsValue] = {
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

  override def getTokenWithAuthCode(authCode: String, clientId: String): Future[JsValue] = {
    val dataParts =
      Map(
        "grant_type" -> Seq("authorization_code"),
        "code" -> Seq(authCode),
        "client_id" -> Seq(clientId),
        "client_secret" -> Seq("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"),
        "redirect_uri" -> Seq("http://localhost:9000/auth/callback")
      )
    wsClient
      .url(s"$authServerHost/realms/${KeycloakAdminRepository.googleAuthRealm}/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map(_.json)
  }
}
