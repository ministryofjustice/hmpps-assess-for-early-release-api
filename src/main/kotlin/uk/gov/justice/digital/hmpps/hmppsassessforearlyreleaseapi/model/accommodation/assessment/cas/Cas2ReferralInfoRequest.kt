package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.accommodation.assessment.cas

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "A request to add CAS 2 referral information")
data class Cas2ReferralInfoRequest(
  @Schema(description = "Areas to avoid information", required = true)
  @NotBlank
  @Size(min = 3, max = 200)
  val areasToAvoidInfo: String,
  @Schema(description = "Supporting info for information", required = true)
  @NotBlank
  @Size(min = 3, max = 200)
  val supportingInfoForReferral: String,
)
