package services

import com.google.inject.ImplementedBy
import infrastructure.keycloak.KeycloakAdminRepository

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakAdminRepository])
trait AuthAdminRepository {
  def createUser(id: UUID, emailId: String, ssoEnabled: Boolean = false): Future[Unit]
  def resetPassword(username: String, password: String): Future[Unit]
}
