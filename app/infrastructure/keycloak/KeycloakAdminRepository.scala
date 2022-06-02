package infrastructure.keycloak

import services.AuthAdminRepository

import java.util.UUID
import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

// todo - custom execution context
@Singleton
class KeycloakAdminRepository @Inject() (client: KeycloakClient)(implicit ec: ExecutionContext)
  extends AuthAdminRepository {

  import KeycloakAdminRepository._

  override def createUser(id: UUID, emailId: String, ssoEnabled: Boolean = false): Future[Unit] =
    Future {
      println("Keycloak admin repo: ")
      if (!ssoEnabled) {
        if (!client.userExists(nativeAuthRealm, emailId)) {
          println("No client, adding new user")
          client.addNewUser(nativeAuthRealm, id, emailId, "", "", ssoEnabled)
        } else {
          println("USER ALREADY EXISTS")
        }
      } else ()
    }

  override def resetPassword(username: String, password: String): Future[Unit] =
    Future {
      client.resetPassword(nativeAuthRealm, username, password)
    }
}

object KeycloakAdminRepository {
  val nativeAuthRealm = "NativeAuth"
}
