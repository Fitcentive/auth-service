package io.fitcentive.auth.domain.errors

import io.fitcentive.sdk.error.DomainError

import java.util.UUID

case class UnrecognizedOidcProviderError(reason: String = "Unrecognized OIDC provider") extends DomainError {
  override def code: UUID = UUID.fromString("e4c0512b-6c33-4cad-bec7-8ff614c1ebec")
}
