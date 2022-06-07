package io.fitcentive.auth.services

import com.google.inject.ImplementedBy
import io.fitcentive.auth.domain.errors.JwtValidationError
import io.fitcentive.auth.infrastructure.jwt.JwtTokenValidationService
import io.circe.Decoder

@ImplementedBy(classOf[JwtTokenValidationService])
trait TokenValidationService {
  def validateJwt[T](
    token: String,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit decoder: Decoder[T]): Either[JwtValidationError, T]
}
