package controllers

import javax.inject._
import api.AuthApi
import domain.{PasswordReset, User}
import infrastructure.actions.AuthAction
import infrastructure.utils.ControllerOps
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject() (authApi: AuthApi, cc: ControllerComponents, authAction: AuthAction)(implicit
  exec: ExecutionContext
) extends AbstractController(cc)
  with ControllerOps {

  def validateToken: Action[AnyContent] =
    authAction.async { implicit userRequest =>
      Future.successful(Ok("Success"))
    }

  def createNewUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User](request.body.asJson.get) { user =>
        authApi
          .createNewUser(user)
          .map {
            case Left(error) => domainErrorHandler(error)
            case Right(_)    => Ok("Successful")
          }
          .recover(resultErrorAsyncHandler)
      }
    }

  def resetPassword: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[PasswordReset](request.body.asJson.get) { parameters =>
        authApi
          .resetPassword(parameters.username, parameters.password)
          .map(_ => Ok("Successful"))
          .recover(resultErrorAsyncHandler)
      }
    }

  // todo - add mailhog next
  def logout: Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Refresh token required"))) { formData =>
        authApi
          .logout(formData.dataParts("client_id").head, formData.dataParts("refresh_token").head)
          .map(_ => NoContent)
          .recover(resultErrorAsyncHandler)
      }
    }

  def login: Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Username/password required"))) { formData =>
        authApi
          .generateTokenFromCredentials(
            formData.dataParts("username").head,
            formData.dataParts("password").head,
            formData.dataParts("client_id").head
          )
          .map(Ok(_))
          .recover(resultErrorAsyncHandler)
      }
    }

  def oidcCallback(provider: String, sessionState: String, state: String, code: String): Action[AnyContent] =
    Action.async { implicit request =>
      authApi
        .generateTokenFromAuthCode(provider, code, "webapp")
        .map {
          case Right(token) => Ok(token)
          case Left(error)  => domainErrorHandler(error)
        }
        .recover(resultErrorAsyncHandler)
    }

  def ssoLogin(provider: String): Action[AnyContent] =
    Action.async { implicit request =>
      authApi
        .oidcLoginWithRedirect(provider, request)
        .map {
          case Right(result) => result
          case Left(error)   => domainErrorHandler(error)
        }
        .recover(resultErrorAsyncHandler)
    }

  def refreshAccessToken: Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Refresh token required"))) { formData =>
        authApi
          .refreshAccessToken(
            formData.dataParts("realm").head,
            formData.dataParts("client_id").head,
            formData.dataParts("grant_type").head,
            formData.dataParts("refresh_token").head,
          )
          .map(Ok(_))
          .recover(resultErrorAsyncHandler)
      }
    }

}
