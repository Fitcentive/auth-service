package io.fitcentive.auth.infrastructure.settings

import com.typesafe.config.Config
import io.fitcentive.auth.domain.config.{JwtConfig, KeycloakConfig}
import play.api.Configuration
import io.fitcentive.auth.services.SettingsService
import io.fitcentive.sdk.config.ServerConfig

import javax.inject.{Inject, Singleton}

@Singleton
class AppConfigService @Inject() (config: Configuration) extends SettingsService {

  override def serverConfig: ServerConfig = ServerConfig.fromConfig(config.get[Config]("http-server"))

  override def keycloakConfigRaw: Config = config.get[Config]("keycloak")

  override def keycloakConfig: KeycloakConfig = KeycloakConfig.apply(config.get[Config]("keycloak"))

  override def jwtConfig: JwtConfig = JwtConfig.apply(config.get[Config]("jwt"))
}
