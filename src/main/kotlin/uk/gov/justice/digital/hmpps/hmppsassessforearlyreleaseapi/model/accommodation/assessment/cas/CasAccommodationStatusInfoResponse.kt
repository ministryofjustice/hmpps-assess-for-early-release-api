package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas.CasStatus

@Schema(description = "A Response to return CAS assessment state information")
data class CasAccommodationStatusInfoResponse(
  @Schema(description = "The reference/identifier for the CAS assessment ", example = "142", required = true)
  @NotNull
  val reference: Long,
  @Schema(description = "The status for the CAS assessment ", example = "REFERRAL_REFUSED", required = true)
  @NotNull
  val status: CasStatus,
)
