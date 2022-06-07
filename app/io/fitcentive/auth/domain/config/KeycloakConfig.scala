package io.fitcentive.auth.domain.config

import com.typesafe.config.Config

case class KeycloakConfig(
  serverUrl: String,
  googleOidcLoginUrl: String,
  clientId: String,
  clientSecret: String,
  username: String,
  password: String,
  realms: KeycloakAuthRealms,
)

object KeycloakConfig {
  def apply(config: Config): KeycloakConfig =
    new KeycloakConfig(
      serverUrl = config.getString("server-url"),
      googleOidcLoginUrl = config.getString("google-oidc-login-url"),
      clientId = config.getString("client-id"),
      clientSecret = config.getString("client-secret"),
      username = config.getString("username"),
      password = config.getString("password"),
      realms =
        KeycloakAuthRealms(google = config.getString("realms.google"), native = config.getString("realms.native"))
    )
}
