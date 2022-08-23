package io.fitcentive.auth.infrastructure.utils

import io.fitcentive.auth.domain.errors.UnrecognizedOidcProviderError
import io.fitcentive.auth.services.SettingsService
import io.fitcentive.sdk.error.DomainError

trait AuthProviderOps {

  def settingsService: SettingsService

  def getRedirectUri(provider: String, clientId: String): String =
    s"${settingsService.serverConfig.host}/api/auth/$provider/callback/$clientId"

  def providerToExternalLoginUrl(provider: String): Either[DomainError, String] = {
    val serverUrl = settingsService.keycloakConfig.externalServerUrl
    provider match {
      case "google"   => Right(s"$serverUrl/${settingsService.keycloakConfig.googleOidcLoginUrl}")
      case "apple"    => Right(s"$serverUrl/${settingsService.keycloakConfig.appleOidcLoginUrl}")
      case "facebook" => Right(s"$serverUrl/${settingsService.keycloakConfig.facebookOidcLoginUrl}")
      case _          => Left(UnrecognizedOidcProviderError())
    }
  }

  def providerToRealm(providerOpt: Option[String]): Either[DomainError, String] = {
    providerOpt match {
      case Some(provider) =>
        provider match {
          case "google"   => Right(settingsService.keycloakConfig.realms.google)
          case "apple"    => Right(settingsService.keycloakConfig.realms.apple)
          case "facebook" => Right(settingsService.keycloakConfig.realms.facebook)
          case _          => Left(UnrecognizedOidcProviderError())
        }
      case None =>
        Right(settingsService.keycloakConfig.realms.native)
    }
  }

  def authServerHost: String =
    settingsService.keycloakConfig.internalServerUrl

  def nativeAuthProviderRealm: String =
    settingsService.keycloakConfig.realms.native
}
