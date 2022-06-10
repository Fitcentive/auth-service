package io.fitcentive.auth.domain

import io.circe.Decoder.Result
import io.circe.{Decoder, HCursor}

case class AuthorizedUserWithoutId(username: String, firstName: String, lastName: String, email: String)

object AuthorizedUserWithoutId {
  implicit val decoder: Decoder[AuthorizedUserWithoutId] = new Decoder[AuthorizedUserWithoutId] {
    final def apply(c: HCursor): Result[AuthorizedUserWithoutId] =
      for {
        username <- c.downField("preferred_username").as[String]
        firstName <- c.downField("given_name").as[String]
        lastName <- c.downField("family_name").as[String]
        email <- c.downField("email").as[String]
      } yield AuthorizedUserWithoutId(username, firstName, lastName, email)
  }
}
