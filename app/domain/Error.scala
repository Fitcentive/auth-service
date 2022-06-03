package domain

import java.util.UUID

trait Error {
  def code: UUID
  def reason: String
}
