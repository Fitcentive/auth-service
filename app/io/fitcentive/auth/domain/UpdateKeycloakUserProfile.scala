package io.fitcentive.auth.domain

import play.api.libs.json.{Json, Reads, Writes}

case class UpdateKeycloakUserProfile(email: String, authProvider: String, firstName: String, lastName: String)

object UpdateKeycloakUserProfile {
  implicit lazy val reads: Reads[UpdateKeycloakUserProfile] = Json.reads[UpdateKeycloakUserProfile]
  implicit lazy val writes: Writes[UpdateKeycloakUserProfile] = Json.writes[UpdateKeycloakUserProfile]
}
