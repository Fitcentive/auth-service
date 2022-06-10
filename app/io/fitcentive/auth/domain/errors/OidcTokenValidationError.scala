package io.fitcentive.auth.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class OidcTokenValidationError(reason: String) extends DomainError {
  override def code: UUID = UUID.fromString("b350f5a7-3b84-4622-aea6-6936d4e10821")
}
