package io.fitcentive.auth.domain

import play.api.libs.json._

import java.util.UUID

case class BasicAuthKeycloakUser(userId: UUID, email: String, firstName: String, lastName: String)

object BasicAuthKeycloakUser {
  implicit lazy val reads: Reads[BasicAuthKeycloakUser] = Json.reads[BasicAuthKeycloakUser]
  implicit lazy val writes: Writes[BasicAuthKeycloakUser] = Json.writes[BasicAuthKeycloakUser]
}
