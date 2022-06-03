package infrastructure.keycloak

import io.circe.Json
import org.keycloak.adapters.KeycloakDeploymentBuilder
import services.{PublicKeyRepository, SettingsService}

import java.io.ByteArrayInputStream
import java.security.PublicKey
import javax.inject.{Inject, Singleton}

// todo - cache this in memory
@Singleton
class KeycloakPublicKeyRepository @Inject() (settingsService: SettingsService) extends PublicKeyRepository {
  override def get(realm: String, kid: String): Option[PublicKey] = {
    val jsonConfig = adapterConfiguration(realm, settingsService.keycloakConfig.serverUrl)
    val is = new ByteArrayInputStream(jsonConfig.noSpaces.getBytes)
    val deployment = KeycloakDeploymentBuilder.build(is)

    val locator = deployment.getPublicKeyLocator
    val publicKey = locator.getPublicKey(kid, deployment)
    Option(publicKey)
  }

  private def adapterConfiguration(realm: String, serverUrl: String): Json =
    Json.obj(
      "auth-server-url" -> Json.fromString(serverUrl),
      "realm" -> Json.fromString(realm),
      "resource" -> Json.fromString("ignored"),
    )
}
