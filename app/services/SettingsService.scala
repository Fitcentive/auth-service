package services

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import domain.{JwtConfig, KeycloakConfig}
import infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def keycloakConfig: KeycloakConfig
  def keycloakConfigRaw: Config
  def jwtConfig: JwtConfig
}
