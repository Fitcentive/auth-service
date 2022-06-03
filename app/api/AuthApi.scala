package api

import domain.User
import play.api.libs.json.JsValue
import services.{AuthAdminRepository, AuthTokenRepository}

import javax.inject.Inject
import scala.concurrent.Future

class AuthApi @Inject() (authAdminRepo: AuthAdminRepository, authTokenRepository: AuthTokenRepository) {

  def createNewUser(user: User): Future[Unit] = authAdminRepo.createUser(user)

  def resetPassword(userName: String, password: String): Future[Unit] = authAdminRepo.resetPassword(userName, password)

  def generateToken(userName: String, password: String, clientId: String): Future[JsValue] =
    authTokenRepository.getToken(userName, password, clientId)

  def logout(clientId: String, refreshToken: String): Future[Unit] =
    authTokenRepository.logout(clientId, refreshToken)

}
