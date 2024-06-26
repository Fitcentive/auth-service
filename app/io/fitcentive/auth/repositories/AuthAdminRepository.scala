package io.fitcentive.auth.repositories

import com.google.inject.ImplementedBy
import io.fitcentive.auth.domain.BasicAuthKeycloakUser
import io.fitcentive.auth.infrastructure.keycloak.KeycloakAdminRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakAdminRepository])
trait AuthAdminRepository {
  def createUserWithBasicAuth(user: BasicAuthKeycloakUser): Future[Unit]
  def resetPassword(email: String, password: String): Future[Unit]
  def addAttributesToSsoKeycloakUser(authProviderRealm: String, email: String, userId: UUID): Future[Unit]
  def updateUserProfile(authProviderRealm: String, email: String, firstName: String, lastName: String): Future[Unit]
  def checkIfUserExists(authProviderRealm: String, email: String): Future[Boolean]
  def deleteUser(authProviderRealm: String, email: String): Future[Unit]
}
