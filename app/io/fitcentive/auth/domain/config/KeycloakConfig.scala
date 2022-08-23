package io.fitcentive.auth.domain.config

import com.typesafe.config.Config

case class KeycloakConfig(
  internalServerUrl: String,
  externalServerUrl: String,
  googleOidcLoginUrl: String,
  appleOidcLoginUrl: String,
  facebookOidcLoginUrl: String,
  clientId: String,
  clientSecret: String,
  username: String,
  password: String,
  realms: KeycloakAuthRealms,
)

object KeycloakConfig {
  def apply(config: Config): KeycloakConfig =
    new KeycloakConfig(
      internalServerUrl = config.getString("internal-server-url"),
      externalServerUrl = config.getString("external-server-url"),
      googleOidcLoginUrl = config.getString("google-oidc-login-url"),
      appleOidcLoginUrl = config.getString("apple-oidc-login-url"),
      facebookOidcLoginUrl = config.getString("facebook-oidc-login-url"),
      clientId = config.getString("client-id"),
      clientSecret = config.getString("client-secret"),
      username = config.getString("username"),
      password = config.getString("password"),
      realms = KeycloakAuthRealms(
        google = config.getString("realms.google"),
        apple = config.getString("realms.apple"),
        facebook = config.getString("realms.facebook"),
        native = config.getString("realms.native")
      )
    )
}
