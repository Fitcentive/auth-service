package services

import com.google.inject.ImplementedBy
import infrastructure.keycloak.KeycloakPublicKeyRepository

import java.security.PublicKey

@ImplementedBy(classOf[KeycloakPublicKeyRepository])
trait PublicKeyRepository {
  def get(realm: String, kid: String): Option[PublicKey]
}
