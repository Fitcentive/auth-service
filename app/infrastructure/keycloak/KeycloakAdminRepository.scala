package infrastructure.keycloak

import domain.User
import infrastructure.utils.AuthProviderOps
import play.api.Logger
import services.AuthAdminRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

// todo - custom execution context
@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient, authProviderOps: AuthProviderOps)(implicit
  ec: ExecutionContext
) extends AuthAdminRepository {

  private val logger: Logger = Logger(this.getClass)

  override def createUser(user: User): Future[Unit] = {
    println("here")
    authProviderOps.providerToRealm(user.ssoProvider).map { providerRealm =>
      println(s"Got the provider realm to be: ${providerRealm}")
      if (!client.userExists(providerRealm, user.email))
        client.addNewUser(
          providerRealm,
          user.userId,
          user.email,
          user.firstName,
          user.lastName,
          ssoEnabled = user.ssoProvider.fold(false)(_ => true)
        )
      else {
        println("Skipping over")
        ()
      }
    }
  }

  override def resetPassword(username: String, password: String): Future[Unit] =
    Future {
      client.resetPassword(authProviderOps.nativeAuthProviderRealm, username, password)
    }
}
