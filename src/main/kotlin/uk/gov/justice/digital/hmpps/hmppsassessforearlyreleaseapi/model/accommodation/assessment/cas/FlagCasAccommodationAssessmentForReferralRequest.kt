package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank

@Schema(description = "A request to flag that the CAS assessment is ready for referral", example = "true", required = true)
data class FlagCasAccommodationAssessmentForReferralRequest(
  @Schema(description = "AS assessment is ready for referral", example = "true", required = true)
  @NotBlank
  val isReferred: Boolean,
)
