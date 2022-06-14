package io.fitcentive.auth.infrastructure.keycloak

import cats.data.EitherT
import io.fitcentive.auth.infrastructure.contexts.KeycloakServerExecutionContext
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import play.api.libs.json.JsValue
import play.api.libs.ws.WSClient
import io.fitcentive.auth.services.AuthTokenService
import io.fitcentive.sdk.error.DomainError

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class KeycloakTokenService @Inject() (wsClient: WSClient, authProviderOps: AuthProviderOps)(implicit
  ec: KeycloakServerExecutionContext
) extends AuthTokenService {

  override def refreshAccessToken(realm: String, clientId: String, refreshToken: String): Future[JsValue] = {
    val dataParts =
      Map("grant_type" -> Seq("refresh_token"), "refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(s"${authProviderOps.authServerHost}/realms/$realm/protocol/openid-connect/token")
      .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
      .post(dataParts)
      .map(_.json)
  }

  override def logout(clientId: String, refreshToken: String): Future[Unit] = {
    val dataParts = Map("refresh_token" -> Seq(refreshToken), "client_id" -> Seq(clientId))
    wsClient
      .url(
        s"${authProviderOps.authServerHost}/realms/${authProviderOps.nativeAuthProviderRealm}/protocol/openid-connect/logout"
      )
      .withHttpHeaders("Content-Type" -> "application/x-www-form-urlencoded")
      .post(dataParts)
      .map(_ => ())
  }

  override def getTokenWithAuthCode(
    provider: String,
    authCode: String,
    clientId: String
  ): Future[Either[DomainError, JsValue]] = {
    val dataParts =
      Map(
        "grant_type" -> Seq("authorization_code"),
        "code" -> Seq(authCode),
        "client_id" -> Seq(clientId),
        "redirect_uri" -> Seq(authProviderOps.getRedirectUri(provider, clientId))
      )
    (for {
      providerRealm <-
        EitherT[Future, DomainError, String](Future.successful(authProviderOps.providerToRealm(Some(provider))))
      result <- EitherT.right[DomainError](
        wsClient
          .url(s"${authProviderOps.authServerHost}/realms/$providerRealm/protocol/openid-connect/token")
          .withHttpHeaders(("Content-Type" -> "application/x-www-form-urlencoded"), ("Accept" -> "application/json"))
          .post(dataParts)
      )
    } yield result.json).value
  }
}
