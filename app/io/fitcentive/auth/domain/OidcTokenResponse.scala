package io.fitcentive.auth.domain

import play.api.libs.json.{Json, Reads, Writes}

case class OidcTokenResponse(access_token: String, id_token: String, refresh_token: String)

object OidcTokenResponse {
  implicit lazy val reads: Reads[OidcTokenResponse] = Json.reads[OidcTokenResponse]
  implicit lazy val writes: Writes[OidcTokenResponse] = Json.writes[OidcTokenResponse]
}
