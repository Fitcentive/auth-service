package domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

sealed abstract class JwtValidationError(val reason: String) extends DomainError {
  override def code: UUID =
    JwtValidationError.code
}

// todo - validate on azp
object JwtValidationError {

  val code: UUID = UUID.fromString("c9ab6043-04c9-4134-a6b1-8bf3f2c85f05")

  final case class ExpiredToken(token: String) extends JwtValidationError(s"Token expired: $token")
  final case class PrematureToken(token: String) extends JwtValidationError(s"Premature token: $token")
  final case class NoIssuer(token: String) extends JwtValidationError(s"No issuer in token: $token")
  final case class NoKeyId(token: String) extends JwtValidationError(s"No key id provided: $token")
  final case class UnknownIssuer(issuer: String) extends JwtValidationError(s"Unknown token issuer: $issuer")
  final case class UnknownRealm(realm: String) extends JwtValidationError(s"Unknown realm: $realm")
  final case class BadToken(token: String, cause: String)
    extends JwtValidationError(s"Unsigned, badly signed, or otherwise bad token due to $cause.\n Token \n: $token")

}
