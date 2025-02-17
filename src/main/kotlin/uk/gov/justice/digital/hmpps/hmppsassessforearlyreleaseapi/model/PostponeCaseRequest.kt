package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType

@Schema(description = "Request by case administrator or decision maker to postpone case")
data class PostponeCaseRequest(

  @Schema(description = "The reason or reasons for the case postponement", example = "ON_REMAND", required = true)
  @field:NotNull
  @field:NotEmpty
  @field:Valid
  val reasonTypes: LinkedHashSet<PostponeCaseReasonType> = LinkedHashSet(),

  @Schema(description = "Details of the agent who is requesting to postpone the case", required = true)
  val agent: Agent,
)
