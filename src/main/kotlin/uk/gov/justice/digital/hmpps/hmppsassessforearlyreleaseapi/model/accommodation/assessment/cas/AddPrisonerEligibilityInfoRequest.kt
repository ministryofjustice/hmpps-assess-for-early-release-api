package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

@Schema(description = "A request to add CAS eligibility information")
data class AddPrisonerEligibilityInfoRequest(
  @Schema(description = "Is person eligible for cas", example = "false", required = true)
  @NotNull
  val eligibleForCas: Boolean,
  @Schema(description = "If the person is not eligible reason", required = false)
  @Size(min = 3, max = 200)
  val ineligibilityReason: String? = null,
)
