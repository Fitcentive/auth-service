package io.fitcentive.auth.modules

import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.auth.infrastructure.keycloak.KeycloakClient
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.services.SettingsService

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
