package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks

enum class PolicyVersion {
  V1,
}

enum class ResidentialChecksStatus {
  NOT_STARTED,
  IN_PROGRESS,
  UNSUITABLE,
  SUITABLE,
}

enum class TaskStatus {
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
  val name: String,
  val type: InputType,
  val options: List<Option>? = null,
)

data class TaskQuestion(
  val code: String,
  val text: String,
  val hintText: String? = null,
  val input: Input,
)

data class Section(
  val header: String? = null,
  val hintText: String? = null,
  val questions: List<TaskQuestion>,
)

data class Task(
  val code: String,
  val name: String,
  val sections: List<Section>,
)

data class ResidentialChecksPolicy(
  val version: PolicyVersion,
  val tasks: List<Task>,
)
