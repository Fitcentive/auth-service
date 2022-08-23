package io.fitcentive.auth.infrastructure.rest

import io.fitcentive.auth.domain.{PasswordReset, User}
import io.fitcentive.auth.infrastructure.contexts.KeycloakServerExecutionContext
import io.fitcentive.auth.infrastructure.rest.RestUserService.{CreateNewAppUserPayload, UpdateUserProfilePayload}
import io.fitcentive.auth.services.{SettingsService, UserService}
import io.fitcentive.sdk.config.ServerConfig
import play.api.http.Status
import play.api.libs.json.{Json, Writes}
import play.api.libs.ws.{WSClient, WSRequest}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.Future
import scala.util.chaining.scalaUtilChainingOps

class RestUserService @Inject() (wsClient: WSClient, settingsService: SettingsService)(implicit
  ec: KeycloakServerExecutionContext
) extends UserService {

  private val userServiceConfig: ServerConfig = settingsService.userServiceConfig
  val baseUrl: String = userServiceConfig.serverUrl

  override def updateUserProfile(userId: UUID, firstName: Option[String], lastName: Option[String]): Future[Unit] =
    wsClient
      .url(s"$baseUrl/api/internal/user/$userId/profile")
      .addHttpHeaders("Accept" -> "application/json")
      .addServiceSecret
      .post(Json.toJson(UpdateUserProfilePayload(firstName, lastName)))
      .map(_ => ())

  override def createSsoUser(email: String, ssoProvider: String): Future[User] =
    wsClient
      .url(s"$baseUrl/api/internal/user/sso")
      .withQueryStringParameters("email" -> email)
      .addHttpHeaders("Accept" -> "application/json")
      .addServiceSecret
      .post(Json.toJson(CreateNewAppUserPayload(email, ssoProvider)))
      .map(_.json.as[User])

  override def getUserByEmailAndRealm(email: String, providerRealm: String): Future[Option[User]] =
    wsClient
      .url(s"$baseUrl/api/internal/user/email")
      .withQueryStringParameters("email" -> email, "realm" -> providerRealm)
      .addHttpHeaders("Accept" -> "application/json")
      .addServiceSecret
      .get()
      .map { response =>
        response.status match {
          case Status.OK        => response.json.as[User].pipe(Some.apply)
          case Status.NOT_FOUND => None
        }
      }

  override def getUserByEmail(email: String): Future[Option[User]] =
    wsClient
      .url(s"$baseUrl/api/internal/user/email-only")
      .withQueryStringParameters("email" -> email)
      .addHttpHeaders("Accept" -> "application/json")
      .addServiceSecret
      .get()
      .map { response =>
        response.status match {
          case Status.OK        => response.json.as[User].pipe(Some.apply)
          case Status.NOT_FOUND => None
        }
      }

  implicit class ServiceSecretHeaders(wsRequest: WSRequest) {
    def addServiceSecret: WSRequest =
      wsRequest.addHttpHeaders("Service-Secret" -> settingsService.secretConfig.serviceSecret)
  }
}

object RestUserService {

  case class CreateNewAppUserPayload(email: String, ssoProvider: String)
  object CreateNewAppUserPayload {
    implicit lazy val writes: Writes[CreateNewAppUserPayload] = Json.writes[CreateNewAppUserPayload]
  }

  case class UpdateUserProfilePayload(firstName: Option[String], lastName: Option[String])
  object UpdateUserProfilePayload {
    implicit lazy val writes: Writes[UpdateUserProfilePayload] = Json.writes[UpdateUserProfilePayload]
  }
}
