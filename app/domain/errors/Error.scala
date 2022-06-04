package domain.errors

import java.util.UUID

trait Error {
  def code: UUID
  def reason: String
}
