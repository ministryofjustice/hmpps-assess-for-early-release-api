package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(
  description = "The initial checks for a specific assessment",
)
data class InitialChecks(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "all eligibility checks ELIGIBLE, or any eligibility check INELIGIBLE")
  val complete: Boolean = false,

  @Schema(description = "eligibility status = ELIGIBLE and suitability status = SUITABLE")
  val checksPassed: Boolean,

  @Schema(description = "state of current eligibility checks")
  val eligibilityStatus: EligibilityStatus,
  @Schema(description = "details of current eligibility checks")
  val eligibility: List<EligibilityCheckDetails>,

  @Schema(description = "state of current suitability checks")
  val suitabilityStatus: SuitabilityStatus,
  @Schema(description = "details of current suitability checks")
  val suitability: List<SuitabilityCheckDetails>,
)

enum class InitialChecksStatus {
  ELIGIBLE,
  INELIGIBLE,
  NOT_STARTED,
  IN_PROGRESS,
}

enum class EligibilityStatus {
  ELIGIBLE,
  INELIGIBLE,
  NOT_STARTED,
  IN_PROGRESS,
}

enum class SuitabilityStatus {
  SUITABLE,
  UNSUITABLE,
  NOT_STARTED,
  IN_PROGRESS,
}

@Schema(description = "The initial checks for a specific assessment")
data class EligibilityCheckDetails(
  @Schema(description = "the unique code to identify this check", example = "rotl-failure-to-return")
  val code: String,
  @Schema(description = "The name of the check that would appear in a task list", example = "ROTL failure to return")
  val taskName: String,
  @Schema(description = "The state of this check", example = "NOT_STARTED")
  val status: EligibilityStatus,
  @Schema(description = "The questions that are posed to the user")
  val questions: List<Question> = emptyList(),
)

@Schema(description = "The initial checks for a specific assessment")
data class SuitabilityCheckDetails(
  @Schema(description = "the unique code to identify this check", example = "rosh-and-mappa")
  val code: String,
  @Schema(description = "The name of the check that would appear in a task list", example = "RoSH and MAPPA")
  val taskName: String,
  @Schema(description = "The state of this check", example = "NOT_STARTED")
  val status: SuitabilityStatus,
  @Schema(description = "The questions that are posed to the user")
  val questions: List<Question> = emptyList(),
)

@Schema(description = "A question that is asked by the user")
data class Question(
  @Schema(description = "The question that is posed to the user", example = "a question...")
  val text: String,
  @Schema(description = "The hint html associated with this question", example = "<p>Some hint text</p>")
  val hint: String? = null,
  @Schema(description = "The name that the data will be stored under for this check", example = "question1")
  val name: String? = null,
  @Schema(description = "The answer provided by the user", example = "true")
  val answer: Boolean? = null,
)
