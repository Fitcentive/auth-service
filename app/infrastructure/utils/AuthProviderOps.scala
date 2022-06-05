package infrastructure.utils

import services.SettingsService
import domain.errors
import infrastructure.utils.AuthProviderOps.UnrecognizedOidcProvider

import java.util.UUID
import scala.concurrent.Future

trait AuthProviderOps {

  def settingsService: SettingsService

  def getRedirectUri(provider: String, clientId: String): String =
    s"${settingsService.serverConfig.host}/auth/$provider/callback/$clientId"

  def providerToLoginUrl(provider: String): Either[errors.Error, String] = {
    val serverUrl = settingsService.keycloakConfig.serverUrl
    provider match {
      case "google" => Right(s"$serverUrl/${settingsService.keycloakConfig.googleOidcLoginUrl}")
      case _        => Left(UnrecognizedOidcProvider)
    }
  }

  def providerToRealm(providerOpt: Option[String]): Either[errors.Error, String] = {
    providerOpt match {
      case Some(provider) =>
        provider match {
          case "google" => Right(settingsService.keycloakConfig.realms.google)
          case _        => Left(UnrecognizedOidcProvider)
        }
      case None =>
        Right(settingsService.keycloakConfig.realms.native)
    }
  }

  def authServerHost: String =
    settingsService.keycloakConfig.serverUrl

  def nativeAuthProviderRealm: String =
    settingsService.keycloakConfig.realms.native
}

object AuthProviderOps {
  case object UnrecognizedOidcProvider extends domain.errors.Error {
    override def code: UUID = UUID.fromString("e4c0512b-6c33-4cad-bec7-8ff614c1ebec")
    override def reason: String = "Unrecognized OIDC provider"
  }
}
