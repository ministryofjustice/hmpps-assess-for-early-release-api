package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull

@Schema(
  description = "A view on the progress of suitability and eligibility criteria for a specific case",
)
data class EligibilityAndSuitabilityCaseView(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "Overall status of eligibility assessment", example = "IN_PROGRESS")
  val overallStatus: EligibilityStatus,

  @Schema(description = "state of current eligibility checks", example = "NOT_STARTED")
  val eligibilityStatus: EligibilityStatus,
  @Schema(description = "details of current eligibility checks")
  val eligibility: List<EligibilityCriterionProgress>,

  @Schema(description = "state of current suitability checks", example = "NOT_STARTED")
  val suitabilityStatus: SuitabilityStatus,
  @Schema(description = "details of current suitability checks")
  val suitability: List<SuitabilityCriterionProgress>,
  @Schema(description = "The type of the failure", example = "INELIGIBLE")
  val failureType: FailureType?,
  @Schema(description = "Reasons why someone is ineligible")
  val failedCheckReasons: List<String>,
)

@Schema(
  description = "The details of a specific eligibility criterion",
)
data class EligibilityCriterionView(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "progress on this criterion")
  val criterion: EligibilityCriterionProgress,

  @Schema(description = "progress on the next criterion")
  val nextCriterion: EligibilityCriterionProgress?,
)

@Schema(
  description = "The details of a specific suitability criterion",
)
data class SuitabilityCriterionView(
  @Schema(description = "A summary of an offender's current assessment")
  val assessmentSummary: AssessmentSummary,

  @Schema(description = "progress on this criterion")
  val criterion: SuitabilityCriterionProgress,

  @Schema(description = "progress about the next criterion")
  val nextCriterion: SuitabilityCriterionProgress?,
)

enum class FailureType {
  INELIGIBLE,
  UNSUITABLE,
}

enum class EligibilityStatus {
  ELIGIBLE,
  INELIGIBLE,
  IN_PROGRESS,
  NOT_STARTED,
}

enum class SuitabilityStatus {
  SUITABLE,
  UNSUITABLE,
  IN_PROGRESS,
  NOT_STARTED,
}

@Schema(description = "The progress on a specific eligibility criterion for a case")
data class EligibilityCriterionProgress(
  @Schema(description = "the unique code to identify this criterion", example = "rotl-failure-to-return")
  val code: String,
  @Schema(description = "The name of the criterion that would appear in a task list", example = "ROTL failure to return")
  val taskName: String,
  @Schema(description = "Status of this criterion for a specific case", example = "NOT_STARTED")
  val status: EligibilityStatus,
  @Schema(description = "The questions that are associated with this criterion for this case")
  val questions: List<Question> = emptyList(),
  @Schema(description = "Details of the user that submitted the answers for this criterion")
  val agent: Agent?,
)

@Schema(description = "The progress on a specific suitability criteria for a case")
data class SuitabilityCriterionProgress(
  @Schema(description = "the unique code to identify this criterion", example = "rosh-and-mappa")
  val code: String,
  @Schema(description = "The name of the criterion that would appear in a task list", example = "RoSH and MAPPA")
  val taskName: String,
  @Schema(description = "Status of this criterion for a specific case", example = "NOT_STARTED")
  val status: SuitabilityStatus,
  @Schema(description = "The questions that are associated with this criterion for this case")
  val questions: List<Question> = emptyList(),
  @Schema(description = "Details of the user that submitted the answers for this criterion")
  val agent: Agent?,
)

@Schema(description = "A question that is asked by the user")
data class Question(
  @Schema(description = "The question that is posed to the user", example = "a question...")
  val text: String,
  @Schema(description = "The hint html associated with this question", example = "<p>Some hint text</p>")
  val hint: String? = null,
  @Schema(description = "The name that the data will be stored under for this check", example = "question1")
  val name: String? = null,
  @Schema(description = "The answer provided by the user for this question", example = "true")
  val answer: Boolean? = null,
)

enum class CriteriaType {
  @JsonProperty("eligibility")
  ELIGIBILITY,

  @JsonProperty("suitability")
  SUITABILITY,
}

@Schema(description = "The answers to the question for a specific criterion")
data class CriterionCheck(
  @NotNull
  @Schema(description = "The type of criteria", example = "eligibility")
  val type: CriteriaType,
  @NotNull
  @Schema(description = "A unique code for the check", example = "code-1")
  val code: String,

  @NotNull
  @Schema(description = "A unique code for the check")
  val answers: Map<String, Boolean>,

  @Schema(description = "Details of the agent who is requesting the criterion check")
  val agent: Agent,
)
