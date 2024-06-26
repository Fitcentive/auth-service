package io.fitcentive.auth.infrastructure.utils

import io.fitcentive.auth.domain.errors.UnrecognizedOidcProviderError
import play.api.mvc.Result
import io.fitcentive.sdk.error.DomainError
import io.fitcentive.sdk.logging.AppLogger
import io.fitcentive.sdk.utils.DomainErrorHandler
import play.api.mvc.Results.{BadRequest, InternalServerError}

trait ServerErrorHandler extends DomainErrorHandler with AppLogger {

  override def resultErrorAsyncHandler: PartialFunction[Throwable, Result] = {
    case e: Exception =>
      logError(s"${e.getMessage}", e)
      InternalServerError(e.getMessage)
  }

  override def domainErrorHandler: PartialFunction[DomainError, Result] = {
    case UnrecognizedOidcProviderError(reason) => BadRequest(reason)
  }

}
