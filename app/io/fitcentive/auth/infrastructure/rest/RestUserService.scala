package io.fitcentive.auth.infrastructure.rest

import io.fitcentive.auth.domain.{PasswordReset, User}
import io.fitcentive.auth.infrastructure.contexts.KeycloakServerExecutionContext
import io.fitcentive.auth.infrastructure.rest.RestUserService.CreateNewAppUserPayload
import io.fitcentive.auth.services.{SettingsService, UserService}
import io.fitcentive.sdk.config.ServerConfig
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.WSClient

import javax.inject.Inject
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class RestUserService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: KeycloakServerExecutionContext
) extends UserService {

  private val serverConfig: ServerConfig = settingsService.userServiceConfig

  override def createSsoUser(email: String, ssoProvider: String): Future[User] =
    wsClient
      .url(s"${serverConfig.serverUrl}/api/sso-user")
      .withQueryStringParameters("email" -> email)
      .withHttpHeaders("Accept" -> "application/json")
      .post(Json.toJson(CreateNewAppUserPayload(email, ssoProvider)))
      .map(_.json.as[User])

  override def getUserByEmail(email: String): Future[Option[User]] =
    wsClient
      .url(s"${serverConfig.serverUrl}/api/user-by-email")
      .withQueryStringParameters("email" -> email)
      .withHttpHeaders("Accept" -> "application/json")
      .get()
      .map { response =>
        response.status match {
          case Status.OK        => response.json.as[User].pipe(Some.apply)
          case Status.NOT_FOUND => None
        }
      }
}

object RestUserService {

  case class CreateNewAppUserPayload(email: String, ssoProvider: String)

  object CreateNewAppUserPayload {
    implicit lazy val writes: Writes[CreateNewAppUserPayload] = Json.writes[CreateNewAppUserPayload]
  }
}