package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

@Schema(
  description = "A view on the progress of the residential checks for an assessment",
)
data class ResidentialChecksTaskView(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "Details of the task")
  val taskConfig: Task,

  @Schema(description = "The current status of the task")
  val taskStatus: TaskStatus,

  @Schema(description = "A map of answer codes to answer values", implementation = Map::class, ref = "#/components/schemas/MapStringAny")
  val answers: Map<String, Any?>,
)
