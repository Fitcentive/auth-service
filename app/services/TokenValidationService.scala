package services

import com.google.inject.ImplementedBy
import domain.errors.JwtValidationError
import infrastructure.jwt.JwtTokenValidationService
import io.circe.Decoder

@ImplementedBy(classOf[JwtTokenValidationService])
trait TokenValidationService {
  def validateJwt[T](
    token: String,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit decoder: Decoder[T]): Either[JwtValidationError, T]
}
