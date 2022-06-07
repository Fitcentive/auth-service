package io.fitcentive.auth.domain

import play.api.mvc.{Request, WrappedRequest}

case class UserRequest[A](authorizedUser: AuthorizedUser, request: Request[A]) extends WrappedRequest[A](request)
