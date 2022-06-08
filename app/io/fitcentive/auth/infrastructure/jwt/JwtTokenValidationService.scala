package io.fitcentive.auth.infrastructure.jwt

import io.fitcentive.auth.domain.config.JwtConfig
import io.fitcentive.auth.domain.errors.JwtValidationError._
import io.fitcentive.auth.domain.errors.JwtValidationError
import io.circe.Decoder
import io.circe.parser.parse
import io.fitcentive.auth.repositories.PublicKeyRepository
import pdi.jwt.{JwtCirce, JwtClaim, JwtHeader, JwtOptions}
import pdi.jwt.exceptions._
import io.fitcentive.auth.services.{SettingsService, TokenValidationService}

import java.security.PublicKey
import javax.inject.{Inject, Singleton}
import scala.util.matching.Regex

@Singleton
class JwtTokenValidationService @Inject() (settingsService: SettingsService, publicKeyRepository: PublicKeyRepository)
  extends TokenValidationService {

  private implicit val config: JwtConfig = settingsService.jwtConfig

  private val authServer: String = config.issuer
  private val Issuer: Regex = s"^$authServer/auth/realms/([^/]+)$$".r
  private val ExpiryOnly: JwtOptions = JwtOptions(signature = false, expiration = true, notBefore = true)
  private val SignatureOnly: JwtOptions = JwtOptions(signature = true, expiration = false, notBefore = false)

  private def transformError(token: String): Throwable => JwtValidationError = {
    case _: JwtExpirationException => ExpiredToken(token)
    case _: JwtNotBeforeException  => PrematureToken(token)
    case e                         => BadToken(token, e.getMessage)
  }

  private def verifyExpiry(token: String): Either[JwtValidationError, (JwtHeader, JwtClaim, String)] =
    JwtCirce.decodeAll(token, ExpiryOnly).toEither.left.map { transformError(token) }

  private def verifyIssuer(token: String, header: JwtHeader, claim: JwtClaim): Either[JwtValidationError, PublicKey] =
    claim.issuer match {
      case Some(Issuer(realm)) =>
        header.keyId match {
          case None      => Left(NoKeyId(token))
          case Some(kid) => verifyRealm(realm, kid)
        }
      case Some(unknown) => Left(UnknownIssuer(unknown))
      case None          => Left(NoIssuer(token))
    }

  private def verifyRealm(realm: String, kid: String): Either[JwtValidationError, PublicKey] =
    publicKeyRepository.get(realm, kid).toRight(UnknownRealm(realm))

  private def verifySignature(token: String, publicKey: PublicKey): Either[JwtValidationError, JwtClaim] =
    JwtCirce.decode(token, publicKey, SignatureOnly).toEither.left.map { e => BadToken(token, e.getMessage) }

  private def validateJwtClaim(jwtClaim: JwtClaim, token: String)(
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit config: JwtConfig): Either[JwtValidationError, JwtClaim] = {
    val isValid =
      jwtClaim.issuer.fold(false)(_.startsWith(config.issuer)) &&
        subject.fold(true)(sub => jwtClaim.subject.fold(false)(_ == sub)) &&
        audience.fold(true)(aud => jwtClaim.audience.fold(false)(_ == aud))

    if (isValid) Right(jwtClaim)
    else Left(BadToken(token, "Invalid issuer, subject, or audience."))
  }

  def validateJwt[T](
    token: String,
    subject: Option[String] = Option.empty,
    audience: Option[Set[String]] = Option.empty
  )(implicit decoder: Decoder[T]): Either[JwtValidationError, T] = {
    for {
      tuple <- verifyExpiry(token)
      (header, claim, _signature) = tuple
      publicKey <- verifyIssuer(token, header, claim)
      secondRoundClaim <- verifySignature(token, publicKey)
      jwtClaim <- validateJwtClaim(secondRoundClaim, token)()
      jsonClaim <-
        parse(jwtClaim.content).left.map(error => BadToken(token, s"Json Parsing failure: ${error.getMessage}"))
      result <- jsonClaim.as[T].left.map(error => BadToken(token, s"Json decoding failure: ${error.getMessage}"))
    } yield result
  }

}
