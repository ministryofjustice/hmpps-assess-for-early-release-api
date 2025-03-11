package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress

@io.swagger.v3.oas.annotations.media.Schema(description = "Request for updating the case admin additional information for an address check request")
data class UpdateCaseAdminAdditionInfoRequest(
  @io.swagger.v3.oas.annotations.media.Schema(description = "The case admin additional information about an address check request", example = "Additional information...", maxLength = 1000)
  @field:jakarta.validation.constraints.Size(max = 1000, message = "Additional information cannot be longer that 1000 characters.")
  val additionalInformation: String,
)
