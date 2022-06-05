package services

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import domain.config.{JwtConfig, KeycloakConfig, ServerConfig}
import infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def keycloakConfig: KeycloakConfig
  def keycloakConfigRaw: Config
  def jwtConfig: JwtConfig
  def serverConfig: ServerConfig
}
