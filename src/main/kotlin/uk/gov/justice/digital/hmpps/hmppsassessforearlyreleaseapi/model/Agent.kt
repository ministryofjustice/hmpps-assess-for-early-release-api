package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole

@Schema(description = "Details of the agent who is requesting a change be made to a resource")
data class Agent(
  @Schema(description = "The name of the user requesting the change", example = "Bob Smith")
  val username: String,

  @Schema(description = "The role of the user requesting the change", example = "PROBATION_COM")
  val role: UserRole,

  @Schema(
    description = "The organisation the user requesting the change is working on behalf of",
    example = "A prison code or probation team code",
  )
  val onBehalfOf: String? = null,
)

fun Agent?.toEntity(): uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent {
  if (this == null) {
    return SYSTEM_AGENT.toEntity()
  }
  return uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent(
    this.username,
    this.role,
    this.onBehalfOf,
  )
}
