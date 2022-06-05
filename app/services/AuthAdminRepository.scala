package services

import com.google.inject.ImplementedBy
import domain.{errors, User}
import infrastructure.keycloak.KeycloakAdminRepository

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakAdminRepository])
trait AuthAdminRepository {
  def createUser(user: User): Future[Either[errors.Error, Unit]]
  def resetPassword(username: String, password: String): Future[Unit]
}
