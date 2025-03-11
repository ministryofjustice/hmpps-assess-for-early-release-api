package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.AddressPreferencePriority

@Schema(description = "Request for adding a CAS check request")
data class AddCasCheckRequest(
  @Schema(description = "Any additional information added by the case admin", example = "Additional information about this address...")
  val caAdditionalInfo: String?,

  @Schema(description = "Any additional information added by the probation practitioner", example = "Additional information about this address...")
  val ppAdditionalInfo: String?,

  @Schema(description = "The offenders priority for this address", example = "SECOND")
  val preferencePriority: AddressPreferencePriority,
)
