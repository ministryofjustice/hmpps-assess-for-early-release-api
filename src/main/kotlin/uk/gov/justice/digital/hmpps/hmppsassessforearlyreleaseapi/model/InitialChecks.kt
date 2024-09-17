package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
  description = """The initial checks for a specific assessment:
  * eligibility status: any ineligible: ELIGIBLE, INELIGIBLE, IN_PROGRESS, NOT_STARTED
  * suitability status: any unsuitable: SUITABLE, UNSUITABLE, IN_PROGRESS, NOT_STARTED
  * complete: all eligibility checks ELIGIBLE, or any eligibility check INELIGIBLE
  * overall: eligibility status = ELIGIBLE and suitability status = SUITABLE
""",
)
data class InitialChecks(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,
  @Schema(description = "details of current eligibility checks")
  val eligibility: List<EligibilityCheckDetails>,
  @Schema(description = "details of current suitability checks")
  val suitability: List<SuitabilityCheckDetails>,
)

enum class EligibilityState {
  ELIGIBLE,
  INELIGIBLE,
  NOT_STARTED,
  IN_PROGRESS,
}

enum class SuitabilityState {
  ELIGIBLE,
  INELIGIBLE,
  NOT_STARTED,
  IN_PROGRESS,
}

@Schema(description = "The initial checks for a specific assessment")
data class EligibilityCheckDetails(
  @Schema(description = "the unique code to identify this check", example = "rotl-failure-to-return")
  val code: String,
  @Schema(description = "The name of the check that would appear in a task list", example = "ROTL failure to return")
  val taskName: String,
  @Schema(description = "The question that is posed to the user", example = "a question...")
  val question: String,
  @Schema(description = "The state of this check ", example = "NOT_STARTED")
  val state: EligibilityState,
  @Schema(description = "The answer provided by the user", example = "Yes")
  val answer: Any?,
)

@Schema(description = "The initial checks for a specific assessment")
data class SuitabilityCheckDetails(
  @Schema(description = "the unique code to identify this check", example = "rosh-and-mappa")
  val code: String,
  @Schema(description = "The name of the check that would appear in a task list", example = "RoSH and MAPPA")
  val taskName: String,
  @Schema(description = "The question that is posed to the user", example = "a question...")
  val question: String,
  @Schema(description = "The state of this check ", example = "NOT_STARTED")
  val state: SuitabilityState,
  @Schema(description = "The answer provided by the user", example = "Yes")
  val answer: Any?,
)
