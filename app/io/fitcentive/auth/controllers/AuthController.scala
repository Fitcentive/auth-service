package io.fitcentive.auth.controllers

import io.fitcentive.auth.infrastructure.utils.ServerErrorHandler
import io.fitcentive.auth.api.AuthApi
import io.fitcentive.auth.domain.{BasicAuthKeycloakUser, PasswordReset, UpdateKeycloakUserProfile}
import io.fitcentive.sdk.play.{InternalAuthAction, NewSsoUserAuthAction, UserAuthAction}
import io.fitcentive.sdk.utils.PlayControllerOps
import play.api.mvc._

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AuthController @Inject() (
  authApi: AuthApi,
  cc: ControllerComponents,
  userAuthAction: UserAuthAction,
  newSsoUserAuthAction: NewSsoUserAuthAction,
  internalAuthAction: InternalAuthAction
)(implicit exec: ExecutionContext)
  extends AbstractController(cc)
  with PlayControllerOps
  with ServerErrorHandler {

  // -----------------------------
  // Internal Auth routes
  // -----------------------------
  def createNewUser: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[BasicAuthKeycloakUser](request.body.asJson) { user =>
        authApi
          .createNewKeycloakUser(user)
          .map(_ => Created("User created successfully"))
          .recover(resultErrorAsyncHandler)
      }
    }

  def resetPassword: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[PasswordReset](request.body.asJson) { parameters =>
        authApi
          .resetPassword(parameters.email, parameters.password)
          .map(_ => Ok("Successful"))
          .recover(resultErrorAsyncHandler)
      }
    }

  def updateUser: Action[AnyContent] =
    internalAuthAction.async { implicit request =>
      validateJson[UpdateKeycloakUserProfile](request.body.asJson) { user =>
        authApi
          .updateKeycloakUserProfile(user)
          .map(handleEitherResult(_)(_ => Ok("User updated successfully")))
          .recover(resultErrorAsyncHandler)
      }
    }

  // -----------------------------
  // User Auth routes
  // -----------------------------
  def logout: Action[AnyContent] =
    userAuthAction.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Refresh token required"))) { formData =>
        authApi
          .logout(formData.dataParts("client_id").head, formData.dataParts("refresh_token").head)
          .map(_ => NoContent)
          .recover(resultErrorAsyncHandler)
      }
    }

  def refreshAccessToken: Action[AnyContent] =
    userAuthAction.async { implicit request =>
      request.body.asMultipartFormData.fold(Future.successful(BadRequest("Refresh token required"))) { formData =>
        authApi
          .refreshAccessToken(
            formData.dataParts("realm").head,
            formData.dataParts("client_id").head,
            formData.dataParts("refresh_token").head,
          )
          .map(Ok(_))
          .recover(resultErrorAsyncHandler)
      }
    }

  /**
    * New SSO User Auth Route
    */
  def createNewDomainSsoUser(providerRealm: String): Action[AnyContent] =
    newSsoUserAuthAction.async { implicit newSsoUserRequest =>
      authApi
        .createNewDomainSsoUser(newSsoUserRequest.newSsoUser, providerRealm)
        .map(handleEitherResult(_)(_ => Ok("Sso user created!")))
        .recover(resultErrorAsyncHandler)
    }

  // -----------------------------
  // Unauthenticated routes
  // -----------------------------
  // todo - validate on `state`? Might need distributed cache
  def oidcCallback(provider: String, clientId: String, code: String): Action[AnyContent] =
    Action.async { implicit request =>
      authApi
        .generateAccessTokenAndCreateUserIfNeeded(provider, code, clientId)
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

}
