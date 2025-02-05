package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Agent

@Schema(description = "The request type to save a set of answers for a residential checks task.")
data class SaveResidentialChecksTaskAnswersRequest(
  @Schema(description = "The task code for these answers relate to")
  val taskCode: String,

  @Schema(description = "A map of answer codes to answer values", implementation = Map::class, ref = "#/components/schemas/MapStringAny")
  val answers: Map<String, Any>,

  @Schema(description = "Details of the agent that is submitting the answers")
  val agent: Agent,
)
