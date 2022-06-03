package modules

import com.google.inject.{AbstractModule, Provides}
import infrastructure.keycloak.KeycloakClient
import io.circe.Json
import org.keycloak.adapters.{KeycloakDeployment, KeycloakDeploymentBuilder}
import services.SettingsService

import java.io.ByteArrayInputStream
import javax.inject.Singleton

class KeycloakModule extends AbstractModule {

  @Provides
  @Singleton
  def provideKeycloakClient(settingsService: SettingsService): KeycloakClient =
    new KeycloakClient(KeycloakClient.fromConfig(settingsService.keycloakConfigRaw))

}
