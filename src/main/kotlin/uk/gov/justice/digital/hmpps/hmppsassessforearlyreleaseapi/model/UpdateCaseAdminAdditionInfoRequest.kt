package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size

@Schema(description = "Request for updating the case admin additional information for an address check request")
data class UpdateCaseAdminAdditionInfoRequest(
  @Schema(description = "The case admin additional information about an address check request", example = "Additional information...", maxLength = 1000)
  @field:Size(max = 1000, message = "Additional information cannot be longer that 1000 characters.")
  val additionalInformation: String,
)
