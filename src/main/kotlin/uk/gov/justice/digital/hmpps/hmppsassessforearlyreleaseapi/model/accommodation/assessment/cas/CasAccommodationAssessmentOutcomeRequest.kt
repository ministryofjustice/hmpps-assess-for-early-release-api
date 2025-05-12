package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(description = "A request to add CAS outcome information")
data class CasAccommodationAssessmentOutcomeRequest(
  @Schema(description = "Outcome information type", example = "REFERRAL_REFUSED", required = true)
  @NotNull
  val outcomeType: CasOutcomeType,
)
