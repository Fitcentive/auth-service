package infrastructure.keycloak

import domain.User
import play.api.Logger
import services.AuthAdminRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

// todo - custom execution context
@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient)(implicit ec: ExecutionContext)
  extends AuthAdminRepository {

  import KeycloakAdminRepository._

  private val logger: Logger = Logger(this.getClass)

  override def createUser(user: User): Future[Unit] =
    Future {
      if (!user.ssoEnabled) {
        if (!client.userExists(nativeAuthRealm, user.email))
          client.addNewUser(nativeAuthRealm, user.userId, user.email, user.firstName, user.lastName, user.ssoEnabled)
        else ()
      } else ()
    }

  override def resetPassword(username: String, password: String): Future[Unit] =
    Future {
      client.resetPassword(nativeAuthRealm, username, password)
    }
}

object KeycloakAdminRepository {
  // todo - config
  val nativeAuthRealm = "NativeAuth"
  val googleAuthRealm = "GoogleAuth"
}
