package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Agent

@Embeddable
data class Agent(
  val username: String,

  @Enumerated(EnumType.STRING)
  val role: UserRole,

  val onBehalfOf: String,
) {
  companion object {
    val SYSTEM_AGENT = Agent("SYSTEM", UserRole.SYSTEM, "SYSTEM")
  }
}
