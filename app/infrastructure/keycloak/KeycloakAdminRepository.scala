package infrastructure.keycloak

import cats.data.EitherT
import domain.User
import infrastructure.contexts.KeycloakClientExecutionContext
import infrastructure.utils.AuthProviderOps
import play.api.Logger
import services.AuthAdminRepository
import domain.errors

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient, authProviderOps: AuthProviderOps)(implicit
  ec: KeycloakClientExecutionContext
) extends AuthAdminRepository {

  private val logger: Logger = Logger(this.getClass)

  override def createUser(user: User): Future[Either[errors.Error, Unit]] = {
    (for {
      providerRealm <- EitherT(authProviderOps.providerToRealm(user.ssoProvider))
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
