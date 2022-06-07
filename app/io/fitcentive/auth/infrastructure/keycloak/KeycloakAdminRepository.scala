package io.fitcentive.auth.infrastructure.keycloak

import cats.data.EitherT
import io.fitcentive.auth.infrastructure.contexts.KeycloakClientExecutionContext
import io.fitcentive.auth.infrastructure.utils.AuthProviderOps
import io.fitcentive.auth.domain.User
import io.fitcentive.auth.services.AuthAdminRepository
import io.fitcentive.sdk.error.DomainError

import javax.inject._
import scala.concurrent.Future

@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient, authProviderOps: AuthProviderOps)(implicit
  ec: KeycloakClientExecutionContext
) extends AuthAdminRepository {

  override def createUser(user: User): Future[Either[DomainError, Unit]] = {
    (for {
      providerRealm <- EitherT(Future.successful(authProviderOps.providerToRealm(user.ssoProvider)))
      _ = if (!client.userExists(providerRealm, user.email))
        client.addNewUser(
          providerRealm,
          user.userId,
          user.email,
          user.firstName,
          user.lastName,
          ssoEnabled = user.ssoProvider.fold(false)(_ => true)
        )
      else ()
    } yield ()).value
  }

  override def resetPassword(username: String, password: String): Future[Unit] =
    Future {
      client.resetPassword(authProviderOps.nativeAuthProviderRealm, username, password)
    }
}
