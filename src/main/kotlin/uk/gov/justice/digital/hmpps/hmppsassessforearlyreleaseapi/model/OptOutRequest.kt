package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

enum class OptOutReasonType {
  NOWHERE_TO_STAY,
  DOES_NOT_WANT_TO_BE_TAGGED,
  NO_REASON_GIVEN,
  OTHER,
}

@Schema(description = "Request for opting an offender out of assess for early release")
data class OptOutRequest(

  @Schema(description = "The reason the offender is opting out", example = "DOES_NOT_WANT_TO_BE_TAGGED")
  @field:NotNull
  val reasonType: OptOutReasonType,

  @Schema(description = "The reason the offender is opting out if reasonType is other", example = "Does")
  val otherDescription: String? = null,
)
