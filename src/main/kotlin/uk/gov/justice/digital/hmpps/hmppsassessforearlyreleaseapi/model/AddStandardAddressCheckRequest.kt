package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AddressPreferencePriority

@Schema(description = "Request for adding a standard address check request")
data class AddStandardAddressCheckRequest(

  @Schema(description = "Any additional information added by the case admin", example = "Additional information about this address...")
  val caAdditionalInfo: String?,

  @Schema(description = "Any additional information added by the probation practitioner", example = "Additional information about this address...")
  val ppAdditionalInfo: String?,

  @Schema(description = "The offenders priority for this address", example = "SECOND")
  val preferencePriority: AddressPreferencePriority,

  @Schema(description = "The UPRN of the address to check", example = "200010019924")
  val addressUprn: String,
)
