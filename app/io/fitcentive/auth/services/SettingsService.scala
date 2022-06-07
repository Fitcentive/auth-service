package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import io.fitcentive.auth.domain.config.{JwtConfig, KeycloakConfig, ServerConfig}
import io.fitcentive.auth.infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def keycloakConfig: KeycloakConfig
  def keycloakConfigRaw: Config
  def jwtConfig: JwtConfig
  def serverConfig: ServerConfig
}
