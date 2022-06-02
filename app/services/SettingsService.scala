package services

import com.google.inject.ImplementedBy
import com.typesafe.config.Config
import infrastructure.settings.AppConfigService

@ImplementedBy(classOf[AppConfigService])
trait SettingsService {
  def keycloakConfig: Config
}
