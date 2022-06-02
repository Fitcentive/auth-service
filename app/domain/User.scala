package domain

import play.api.libs.json._

import java.util.UUID

case class User(userId: UUID, email: String, firstName: String, lastName: String, ssoEnabled: Boolean)

object User {
  implicit lazy val reads: Reads[User] = Json.reads[User]
  implicit lazy val writes: Writes[User] = Json.writes[User]
}
