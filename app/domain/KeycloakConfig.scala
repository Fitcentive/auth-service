package domain

import com.typesafe.config.Config

case class KeycloakConfig(serverUrl: String, clientId: String, clientSecret: String, username: String, password: String)

object KeycloakConfig {
  def apply(config: Config): KeycloakConfig =
    new KeycloakConfig(
      serverUrl = config.getString("server-url"),
      clientId = config.getString("client-id"),
      clientSecret = config.getString("client-secret"),
      username = config.getString("username"),
      password = config.getString("password"),
    )
}
