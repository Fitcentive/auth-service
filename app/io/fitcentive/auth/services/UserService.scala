package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import io.fitcentive.auth.domain.User
import io.fitcentive.auth.infrastructure.rest.RestUserService

import java.util.UUID
import scala.concurrent.Future

@ImplementedBy(classOf[RestUserService])
trait UserService {
  def getUserByEmailAndRealm(email: String, providerRealm: String): Future[Option[User]]
  def createSsoUser(email: String, ssoProvider: String): Future[User]
  def updateUserProfile(userId: UUID, firstName: Option[String], lastName: Option[String]): Future[Unit]
}
