package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

@Schema(description = "A view on the progress of the residential checks for an assessment")
data class ResidentialChecksView(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "Overall status of residential checks for the assessment", example = "IN_PROGRESS")
  val overallStatus: ResidentialChecksStatus,

  @Schema(description = "Details of current residential checks")
  val tasks: List<ResidentialChecksTaskProgress>,
)

@Schema(description = "The progress on a specific residential checks task for an assessment")
data class ResidentialChecksTaskProgress(
  @Schema(description = "Details of the task")
  val config: Task,

  @Schema(description = "Status of this criterion for a specific case", example = "NOT_STARTED")
  val status: TaskStatus,

  @Schema(description = "A map of answer codes to answer values", implementation = Map::class, ref = "#/components/schemas/MapStringAny")
  val answers: Map<String, Any?>,
)
