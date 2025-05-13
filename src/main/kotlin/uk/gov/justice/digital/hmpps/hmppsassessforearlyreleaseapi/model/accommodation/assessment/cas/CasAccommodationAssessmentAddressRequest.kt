package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "A request to add CAS address information")
data class CasAccommodationAssessmentAddressRequest(
  @Schema(description = "Line 1 address information", example = "Maengwyn", required = true)
  @NotBlank
  @Size(min = 3, max = 100)
  val line1: String,
  @Schema(description = "Line 2 address information", example = "Fishguard Road", required = false)
  @Size(min = 3, max = 100)
  val line2: String?,
  @Schema(description = "Town or city address information", example = "Newport", required = true)
  @NotBlank
  @Size(min = 3, max = 50)
  val townOrCity: String,
  @Schema(description = "PostCode address information", example = "SA420UQ", required = true)
  @NotBlank
  @Size(min = 5, max = 10)
  val postCode: String,
)
