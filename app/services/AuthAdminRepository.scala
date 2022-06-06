package services

import com.google.inject.ImplementedBy
import domain.{errors, User}
import infrastructure.keycloak.KeycloakAdminRepository
import io.fitcentive.sdk.error.DomainError

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakAdminRepository])
trait AuthAdminRepository {
  def createUser(user: User): Future[Either[DomainError, Unit]]
  def resetPassword(username: String, password: String): Future[Unit]
}
