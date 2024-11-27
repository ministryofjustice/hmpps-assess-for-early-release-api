package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks

enum class ResidentialChecksPolicyVersion {
  V1,
}

enum class ResidentialChecksStatus {
  NOT_STARTED,
  IN_PROGRESS,
  UNSUITABLE,
  SUITABLE,
}

enum class ResidentialChecksTaskStatus {
  NOT_STARTED,
  IN_PROGRESS,
  UNSUITABLE,
  SUITABLE,
}

enum class InputType {
  TEXT,
  RADIO,
  DATE,
  ADDRESS,
  CHECKBOX,
}

data class Option(
  val value: String,
)

data class Input(
  val type: InputType,
  val options: List<Option>? = null,
)

data class Question(
  val code: String,
  val text: String,
  val hintText: String? = null,
  val input: Input,
)

data class Section(
  val header: String? = null,
  val hintText: String? = null,
  val questions: List<Question>,
)

data class ResidentialChecksTask(
  val code: String,
  val name: String,
  val sections: List<Section>,
)

data class ResidentialChecks(
  val tasks: List<ResidentialChecksTask>,
  val overallStatus: ResidentialChecksStatus,
  val version: ResidentialChecksPolicyVersion,
)

data class ResidentialChecksPolicy(
  val version: ResidentialChecksPolicyVersion,
  val tasks: List<ResidentialChecksTask>,
)
