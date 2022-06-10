package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import io.fitcentive.auth.domain.User
import io.fitcentive.auth.infrastructure.rest.RestUserService

import scala.concurrent.Future

@ImplementedBy(classOf[RestUserService])
trait UserService {
  def getUserByEmail(email: String): Future[Option[User]]
  def createSsoUser(email: String, ssoProvider: String): Future[User]
}
