package infrastructure.utils

import play.api.Logging
import play.api.mvc.{AbstractController, Result}
import domain.errors
import infrastructure.utils.AuthProviderOps.UnrecognizedOidcProvider
import play.api.libs.json.{JsError, JsSuccess, JsValue, Reads}

import scala.concurrent.{ExecutionContext, Future}

trait ControllerOps extends Logging {

  this: AbstractController =>

  def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {

    case e: Exception =>
      logger.error(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  def handleEitherResult[A](
    result: Either[errors.Error, A]
  )(ifSuccess: A => Result)(implicit ec: ExecutionContext): Result = {
    result match {
      case Left(error)  => domainErrorHandler(error)
      case Right(value) => ifSuccess(value)
    }
  }

  private def domainErrorHandler: PartialFunction[errors.Error, Result] = {
    case UnrecognizedOidcProvider => BadRequest(UnrecognizedOidcProvider.reason)
  }

  def validateJson[A](json: JsValue)(block: A => Future[Result])(implicit reads: Reads[A]): Future[Result] = {
    json.validate[A] match {
      case value: JsSuccess[A] => block(value.get)
      case error: JsError      => Future.successful(BadRequest(s"Failed to validate JSON, error: $error"))
    }
  }

}
