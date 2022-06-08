package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import io.fitcentive.auth.domain.config.{JwtConfig, KeycloakConfig}
import io.fitcentive.auth.infrastructure.settings.AppConfigService
import io.fitcentive.sdk.config.ServerConfig

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def keycloakConfig: KeycloakConfig
  def keycloakConfigRaw: Config
  def jwtConfig: JwtConfig
  def serverConfig: ServerConfig
}
