package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasType

@Schema(description = "A request to add CAS type information")
data class CasAccommodationAssessmentTypeRequest(
  @Schema(description = "CAS type", example = "CAS_2", required = true)
  @NotNull
  val casType: CasType,
)
