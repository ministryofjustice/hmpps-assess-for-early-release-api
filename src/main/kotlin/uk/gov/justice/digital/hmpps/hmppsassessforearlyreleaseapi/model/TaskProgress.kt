package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.TaskStatus

@Schema(description = "The progress on a task")
data class TaskProgress(
  @Schema(description = "The name of an outstanding task", example = "ASSESS_ELIGIBILITY")
  val name: Task,

  @Schema(description = "The state of this task for a specific assessment", example = "Smith")
  val progress: TaskStatus,
)
