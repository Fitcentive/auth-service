package modules

import com.google.inject.{AbstractModule, Provides}
import infrastructure.keycloak.KeycloakClient
import services.SettingsService

import javax.inject.Singleton

class KeycloakModule extends AbstractModule {

  @Provides
  @Singleton
  def provideKeycloakClient(settingsService: SettingsService): KeycloakClient =
    new KeycloakClient(KeycloakClient.fromConfig(settingsService.keycloakConfig))

}
