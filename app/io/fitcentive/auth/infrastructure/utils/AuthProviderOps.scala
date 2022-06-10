package io.fitcentive.auth.infrastructure.utils

import io.fitcentive.auth.domain.errors.UnrecognizedOidcProviderError
import io.fitcentive.auth.services.SettingsService
import io.fitcentive.sdk.error.DomainError

trait AuthProviderOps {

  def settingsService: SettingsService

  def getRedirectUri(provider: String, clientId: String): String =
    s"${settingsService.serverConfig.host}/api/auth/$provider/callback/$clientId"

  def providerToLoginUrl(provider: String): Either[DomainError, String] = {
    val serverUrl = settingsService.keycloakConfig.serverUrl
    provider match {
      case "google" => Right(s"$serverUrl/${settingsService.keycloakConfig.googleOidcLoginUrl}")
      case _        => Left(UnrecognizedOidcProviderError())
    }
  }

  def providerToRealm(providerOpt: Option[String]): Either[DomainError, String] = {
    providerOpt match {
      case Some(provider) =>
        provider match {
          case "google" => Right(settingsService.keycloakConfig.realms.google)
          case _        => Left(UnrecognizedOidcProviderError())
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
