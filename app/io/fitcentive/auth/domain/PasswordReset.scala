package io.fitcentive.auth.domain

import play.api.libs.json._

case class PasswordReset(username: String, password: String)

object PasswordReset {
  implicit lazy val reads: Reads[PasswordReset] = Json.reads[PasswordReset]
  implicit lazy val writes: Writes[PasswordReset] = Json.writes[PasswordReset]
}
