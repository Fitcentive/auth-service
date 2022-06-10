package io.fitcentive.auth.modules

import com.google.inject.{AbstractModule, Provides}
import io.fitcentive.auth.infrastructure.keycloak.KeycloakClient
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.services.SettingsService
import io.fitcentive.sdk.config.JwtConfig
import io.fitcentive.sdk.domain.{PublicKeyRepository, TokenValidationService}
import io.fitcentive.sdk.infrastructure.{JwtTokenValidationService, KeycloakPublicKeyRepository}

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

  /**
    * NOTE: The following 3 providers include sdk dependencies for Auth actions
    *       Without them, you will not be able to invoke authentication actions and will run into runtime Guice errors
    */

  @Provides
  @Singleton
  def provideJwtConfig(settingsService: SettingsService): JwtConfig =
    settingsService.jwtConfig

  @Provides
  @Singleton
  // todo - cache somehow
  def provideKeycloakPublicKeyRepository(settingsService: SettingsService): PublicKeyRepository =
    new KeycloakPublicKeyRepository(settingsService.keycloakConfig.serverUrl)

  @Provides
  @Singleton
  def provideJwtTokenValidationService(
    settingsService: SettingsService,
    publicKeyRepository: PublicKeyRepository
  ): TokenValidationService =
    new JwtTokenValidationService(settingsService.jwtConfig, publicKeyRepository)

}
