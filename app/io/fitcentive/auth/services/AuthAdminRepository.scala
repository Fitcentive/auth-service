package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import io.fitcentive.auth.infrastructure.keycloak.KeycloakAdminRepository
import io.fitcentive.auth.domain.User
import io.fitcentive.sdk.error.DomainError

import scala.concurrent.Future

@ImplementedBy(classOf[KeycloakAdminRepository])
trait AuthAdminRepository {
  def createUser(user: User): Future[Either[DomainError, Unit]]
  def resetPassword(username: String, password: String): Future[Unit]
}
