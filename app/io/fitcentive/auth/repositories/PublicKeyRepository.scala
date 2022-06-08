package io.fitcentive.auth.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.auth.infrastructure.keycloak.KeycloakPublicKeyRepository

import java.security.PublicKey

@ImplementedBy(classOf[KeycloakPublicKeyRepository])
trait PublicKeyRepository {
  def get(realm: String, kid: String): Option[PublicKey]
}
