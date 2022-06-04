package infrastructure.utils

import services.SettingsService

import scala.concurrent.Future

trait AuthProviderOps {

  def settingsService: SettingsService

  def providerToLoginUrl(provider: String): Future[String] = {
    val serverUrl = settingsService.keycloakConfig.serverUrl
    provider match {
      case "google" => Future.successful(s"$serverUrl/${settingsService.keycloakConfig.googleOidcLoginUrl}")
      case _        => Future.failed(new Exception("Unrecognized OIDC provider"))
    }
  }

  // todo - either types?
  def providerToRealm(providerOpt: Option[String]): Future[String] = {
    providerOpt match {
      case Some(provider) =>
        provider match {
          case "google" => Future.successful(settingsService.keycloakConfig.realms.google)
          case _        => Future.failed(new Exception("Unrecognized OIDC provider"))
        }

      case None =>
        Future.successful(settingsService.keycloakConfig.realms.native)
    }
  }

  def authServerHost: String =
    settingsService.keycloakConfig.serverUrl

  def nativeAuthProviderRealm: String =
    settingsService.keycloakConfig.realms.native
}
