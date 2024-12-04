package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

@Schema(
  description = "A view on the progress of the residential checks for an assessment",
)
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
  @Schema(description = "The unique code to identify this task", example = "address-details-and-informed-consent")
  val code: String,

  @Schema(
    description = "The name of the check as it would appear in a task list",
    example = "Address details and informed consent",
  )
  val taskName: String,

  @Schema(description = "Status of this criterion for a specific case", example = "NOT_STARTED")
  val status: TaskStatus,
)
