package controllers

import javax.inject._
import api.AuthApi
import domain.{PasswordReset, User}
import infrastructure.actions.AuthAction
import play.api.mvc._
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}

import scala.concurrent.{ExecutionContext, Future}

/**
  * This controller creates an `Action` that demonstrates how to write
  * simple asynchronous code in a controller. It uses a timer to
  * asynchronously delay sending a response for 1 second.
  *
  * @param cc standard controller components
  * @param actorSystem We need the `ActorSystem`'s `Scheduler` to
  * run code after a delay.
  * @param exec We need an `ExecutionContext` to execute our
  * asynchronous code.  When rendering content, you should use Play's
  * default execution context, which is dependency injected.  If you are
  * using blocking operations, such as database or network access, then you should
  * use a different custom execution context that has a thread pool configured for
  * a blocking API.
  */
@Singleton
class AuthController @Inject() (authApi: AuthApi, cc: ControllerComponents, authAction: AuthAction)(implicit
  exec: ExecutionContext
) extends AbstractController(cc) {

  def validateToken: Action[AnyContent] =
    authAction.async { implicit userRequest =>
      println("Successfully validated token via auth action")
      println(userRequest.authorizedUser)
      Future.successful(Ok("Success"))
    }

  def createNewUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User](request.body.asJson.get) { user =>
        println(s"Requested creating of new user for ${user}")
        authApi.createNewUser(user).map(_ => Ok("Successful"))
      }
    }

  def resetPassword: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[PasswordReset](request.body.asJson.get) { parameters =>
        authApi.resetPassword(parameters.username, parameters.password).map(_ => Ok("Successful"))
      }
    }

  // todo - this needs fixing
  // todo - add a recovery handler
  // after this, do refresh flow
  // and then SSO
  // and then mailhog
  def logout: Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Refresh token required"))) { formData =>
        println("About to use authApi")
        authApi
          .logout(formData.dataParts("client_id").head, formData.dataParts("refresh_token").head)
          .map(_ => NoContent)
      }
    }

  def generateToken: Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Username/password required"))) { formData =>
        authApi
          .generateToken(
            formData.dataParts("username").head,
            formData.dataParts("password").head,
            formData.dataParts("client_id").head
          )
          .map(Ok(_))
      }
    }

  def validateJson[A](json: JsValue)(block: A => Future[Result])(implicit reads: Reads[A]): Future[Result] = {
    json.validate[A] match {
      case value: JsSuccess[A] => block(value.get)
      case error: JsError      => Future.successful(BadRequest(s"Failed to validate JSON, error: $error"))
    }
  }

}
