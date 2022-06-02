package infrastructure.settings

import com.typesafe.config.Config
import play.api.Configuration
import services.SettingsService

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration) extends SettingsService {
  override def keycloakConfig: Config =
    config.get[Config]("keycloak")
}
