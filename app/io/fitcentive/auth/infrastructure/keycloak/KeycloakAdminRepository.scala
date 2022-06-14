package io.fitcentive.auth.infrastructure.keycloak

import io.fitcentive.auth.infrastructure.contexts.KeycloakClientExecutionContext
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.domain.BasicAuthKeycloakUser
import io.fitcentive.auth.repositories.AuthAdminRepository

import java.util.UUID
import javax.inject._
import scala.concurrent.Future

@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient, authProviderOps: AuthProviderOps)(implicit
  ec: KeycloakClientExecutionContext
) extends AuthAdminRepository {

  override def checkIfUserExists(authProviderRealm: String, email: String): Future[Boolean] =
    Future {
      client.userExists(authProviderRealm, email)
    }

  override def updateUserProfile(
    authProviderRealm: String,
    email: String,
    firstName: String,
    lastName: String
  ): Future[Unit] =
    Future {
      client.updateUserProfile(authProviderRealm, email, firstName, lastName)
    }

  override def createUserWithBasicAuth(user: BasicAuthKeycloakUser): Future[Unit] =
    for {
      providerRealm <- Future.successful(authProviderOps.nativeAuthProviderRealm)
      _ = if (!client.userExists(providerRealm, user.email))
        client.addNewUser(providerRealm, user.userId, user.email, user.firstName, user.lastName)
      else ()
    } yield ()

  override def resetPassword(email: String, password: String): Future[Unit] =
    Future {
      client.resetPassword(authProviderOps.nativeAuthProviderRealm, email, password)
    }

  override def addAttributesToSsoKeycloakUser(authProviderRealm: String, email: String, userId: UUID): Future[Unit] =
    Future {
      client.addAttributesToUser(authProviderRealm, email, userId)
    }
}
