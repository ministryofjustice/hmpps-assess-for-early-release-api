package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "The answers to a residential checks task.")
data class ResidentialChecksTaskAnswersSummary(
  @Schema(description = "A unique identifier the address check request associated with these answers")
  val addressCheckRequestId: Long,

  @Schema(description = "The task code for these answers relate to")
  val taskCode: String,

  @Schema(description = "A map of answer codes to answer values", implementation = Map::class, ref = "#/components/schemas/MapStringAny")
  val answers: Map<String, Any>,

  @Schema(description = "The version of the task these answers relate to")
  val taskVersion: String,
)
