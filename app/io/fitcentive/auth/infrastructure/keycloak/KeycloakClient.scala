package io.fitcentive.auth.infrastructure.keycloak

import com.typesafe.config.Config
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.keycloak.admin.client.{Keycloak, KeycloakBuilder}
import org.keycloak.representations.idm.{CredentialRepresentation, UserRepresentation}
import org.passay.{CharacterRule, EnglishCharacterData, PasswordGenerator}
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.sdk.logging.AppLogger

import java.security.SecureRandom
import java.time.Instant
import java.util.{Base64, UUID}
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import scala.jdk.CollectionConverters._
import scala.util.Random
import scala.util.chaining.scalaUtilChainingOps

class KeycloakClient(keycloak: Keycloak) extends AppLogger {

  import KeycloakClient._

  /**
    * Add a new user, via the Keycloak client provided.
    * @param userId will be the domain user id - it will be just an attribute.
    *               Keycloak assigns its own unique id to the user.
    */
  def addNewUser(
    realm: String,
    userId: UUID,
    email: String,
    firstName: String,
    lastName: String,
    ssoEnabled: Boolean = false
  ): Unit = {
    val rep = new UserRepresentation()
    rep.setId(userId.toString)
    rep.setCreatedTimestamp(Instant.now().toEpochMilli)
    rep.setUsername(email)
    rep.setEnabled(true)
    rep.setTotp(false)
    rep.setEmailVerified(false)
    rep.setEmail(email)
    rep.setFirstName(firstName)
    rep.setLastName(lastName)
    rep.singleAttribute(userIdUserAttributeKey, userId.toString)
    rep.singleAttribute(realmUserAttributeKey, "NativeAuth")
    if (!ssoEnabled) rep.setRequiredActions(List("UPDATE_PASSWORD").asJava)
    rep.setRealmRoles(List("uma_authorization", "offline_access").asJava)
    rep.setClientRoles(Map("account" -> List("manage-account", "view-profile").asJava).asJava)
    val (creds, _) = credentialFactory()
    rep.setCredentials(List(creds).asJava)
    val users = keycloak.realm(realm).users()
    users.create(rep)
  }

  def resetPassword(realm: String, username: String, password: String): Either[DomainError, Unit] = {
    val users = keycloak.realm(realm).users()
    val userOpt = users.search(username).asScala.find(_.getUsername == username)

    userOpt match {
      case None =>
        logError(s"[KEYCLOAK] resetPassword Could not find user with username $username on realm $realm")
        Left(KeycloakUserNotFound)
      case Some(user) =>
        val credentials = new CredentialRepresentation
        credentials.setValue(password)
        credentials.setTemporary(false)
        credentials.setType(CredentialRepresentation.PASSWORD)
        users.get(user.getId).resetPassword(credentials)
        user.setRequiredActions(List.empty.asJava)
        users.get(user.getId).update(user).pipe(Right.apply)
    }
  }

  def userExists(realm: String, username: String): Boolean = {
    val users = keycloak.realm(realm).users()
    users.search(username).asScala.exists(_.getUsername == username)
  }

  def addAttributesToUser(realm: String, username: String, userId: UUID): Unit =
    updateUserInKeycloak(realm, username) { rep =>
      rep.singleAttribute(userIdUserAttributeKey, userId.toString)
      rep.singleAttribute(realmUserAttributeKey, realm)
    }

  private def updateUserInKeycloak(realm: String, username: String)(
    updateFunc: UserRepresentation => UserRepresentation
  ): Unit = {
    val usersResource = keycloak.realm(realm).users()
    val user = usersResource.search(username).asScala.find(_.getUsername == username).get
    val updatedUser = updateFunc(user)
    usersResource.get(user.getId).update(updatedUser)
  }

}

object KeycloakClient {

  case object KeycloakUserNotFound extends DomainError {
    override def code: UUID = UUID.fromString("8d2569ef-a4ff-43e5-aa71-02e9527076bf")
    override def reason: String = "Keycloak user not found"
  }

  private val userIdUserAttributeKey = "user_id"
  private val realmUserAttributeKey = "auth_realm"

  private val CommonSpecialCharacters: org.passay.CharacterData = new org.passay.CharacterData() {
    override def getErrorCode: String = "INSUFFICIENT_COMMON_SPECIAL"
    override def getCharacters: String = "!%@#$"
  }

  private val PBKDF2: String = "PBKDF2WithHmacSHA1"

  private lazy val secretKeyFactory = SecretKeyFactory.getInstance(PBKDF2)

  private def pbkdf2(password: Array[Char], salt: Array[Byte], iterations: Int, length: Int): Array[Byte] = {
    val spec = new PBEKeySpec(password, salt, iterations, length)
    secretKeyFactory.generateSecret(spec).getEncoded
  }

  lazy val secureRandom: SecureRandom = {
    val sr = new SecureRandom
    sr.setSeed(Random.nextLong())
    sr
  }

  lazy val base64: Base64.Encoder = Base64.getEncoder

  lazy val passay: PasswordGenerator = new PasswordGenerator()

  val passwordLength = 12
  require(passwordLength % 4 == 0, "Invalid password length")

  lazy val charRules: java.util.List[CharacterRule] = {
    val len = passwordLength / 4
    List(
      new CharacterRule(EnglishCharacterData.UpperCase, len),
      new CharacterRule(EnglishCharacterData.LowerCase, len),
      new CharacterRule(EnglishCharacterData.Digit, len),
      new CharacterRule(CommonSpecialCharacters, len)
    ).asJava
  }

  val hashIterations: Int = 20000

  private def generatePassword() = {
    passay.generatePassword(passwordLength, charRules)
  }

  /**
    * Generate a Keycloak Credential object along with the plain text password.
    * @param clearText Clear text password, generates a random password if argument not provided.
    * @return Tuple of (credentials, clearText).
    */
  def credentialFactory(clearText: String = generatePassword()): (CredentialRepresentation, String) = {
    val salt = secureRandom.generateSeed(16)
    val salt_b64 = new String(base64.encode(salt))
    val hashValue = pbkdf2(clearText.toCharArray, salt, hashIterations, 512)
    val hashValue_b64 = new String(base64.encode(hashValue))
    val credentials = new CredentialRepresentation

    credentials.setValue(clearText)
    credentials.setType(CredentialRepresentation.PASSWORD)
    credentials.setTemporary(false)
    credentials -> clearText
  }

  def fromConfig(config: Config): Keycloak = {
    val serverUrl = config.getString("server-url")
    val realm = "master"
    val username = config.getString("username")
    val password = config.getString("password")
    val clientId = config.getString("client-id")
    val clientSecret = config.getString("client-secret")

    KeycloakBuilder
      .builder()
      .serverUrl(serverUrl)
      .realm(realm)
      .username(username)
      .password(password)
      .clientId(clientId)
      .clientSecret(clientSecret)
      .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
      .build()
  }
}
