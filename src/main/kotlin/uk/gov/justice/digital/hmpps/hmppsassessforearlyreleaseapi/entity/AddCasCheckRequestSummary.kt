package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request for adding a CAS check request")
data class AddCasCheckRequestSummary(
  @Schema(description = "Any additional information added by the case admin", example = "Additional information about this address...")
  val caAdditionalInfo: String?,

  @Schema(description = "Any additional information added by the probation practitioner", example = "Additional information about this address...")
  val ppAdditionalInfo: String?,

  @Schema(description = "The offenders priority for this address", example = "SECOND")
  val preferencePriority: AddressPreferencePriority,
)
