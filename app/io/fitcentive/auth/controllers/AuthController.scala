package io.fitcentive.auth.controllers

import io.fitcentive.auth.infrastructure.actions.AuthAction
import io.fitcentive.auth.infrastructure.utils.ServerErrorHandler
import io.fitcentive.auth.api.AuthApi
import io.fitcentive.auth.domain.{PasswordReset, User}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject() (authApi: AuthApi, cc: ControllerComponents, authAction: AuthAction)(implicit
  exec: ExecutionContext
) extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  def validateToken: Action[AnyContent] =
    authAction.async { implicit userRequest =>
      Future.successful(Ok("Success"))
    }

  def createNewUser: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[User](request.body.asJson) { user =>
        authApi
          .createNewUser(user)
          .map(handleEitherResult(_)(_ => Created("User created successfully")))
          .recover(resultErrorAsyncHandler)
      }
    }

  def resetPassword: Action[AnyContent] =
    Action.async { implicit request =>
      validateJson[PasswordReset](request.body.asJson) { parameters =>
        authApi
          .resetPassword(parameters.username, parameters.password)
          .map(_ => Ok("Successful"))
          .recover(resultErrorAsyncHandler)
      }
    }

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

  // todo - validate on `state`? Might need distributed cache
  def oidcCallback(provider: String, clientId: String, code: String): Action[AnyContent] =
    Action.async { implicit request =>
      authApi
        .generateTokenFromAuthCode(provider, code, clientId)
        .map(handleEitherResult(_)(token => Ok(token)))
        .recover(resultErrorAsyncHandler)
    }

  def ssoLogin(provider: String): Action[AnyContent] =
    Action.async { implicit request =>
      authApi
        .oidcLoginWithRedirect(provider, request)
        .map(handleEitherResult(_)(identity))
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