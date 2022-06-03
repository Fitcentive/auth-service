package infrastructure.settings

import com.typesafe.config.Config
import domain.{JwtConfig, KeycloakConfig}
import play.api.Configuration
import services.SettingsService

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration) extends SettingsService {

  override def keycloakConfigRaw: Config = config.get[Config]("keycloak")

  override def keycloakConfig: KeycloakConfig =
    KeycloakConfig.apply(config.get[Config]("keycloak"))

  override def jwtConfig: JwtConfig =
    JwtConfig.apply(config.get[Config]("jwt"))
}
