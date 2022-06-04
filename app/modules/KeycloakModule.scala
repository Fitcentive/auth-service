package modules

import com.google.inject.{AbstractModule, Provides}
import infrastructure.keycloak.KeycloakClient
import infrastructure.utils.AuthProviderOps
import services.SettingsService

import javax.inject.Singleton

class KeycloakModule extends AbstractModule {

  @Provides
  @Singleton
  def provideKeycloakClient(settingsService: SettingsService): KeycloakClient =
    new KeycloakClient(KeycloakClient.fromConfig(settingsService.keycloakConfigRaw))

  @Provides
  @Singleton
  def provideAuthProviderOps(_settingsService: SettingsService): AuthProviderOps =
    new AuthProviderOps {
      override def settingsService: SettingsService = _settingsService
    }

}
