package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks

import com.fasterxml.jackson.annotation.JsonIgnore

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
  val text: String,
  val value: String = text,
)

data class Input(
  val name: String,
  val type: InputType,
  val options: List<Option>? = null,
)

fun interface CriterionMet {
  fun evaluate(answer: Any?): Boolean
}

data class TaskQuestion(
  val code: String,
  val text: String,
  val hintText: String? = null,
  val input: Input,
  @JsonIgnore
  val criterionMet: CriterionMet,
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

enum class VisitedAddress(val displayText: String) {
  I_HAVE_VISITED_THIS_ADDRESS_AND_SPOKEN_TO_THE_MAIN_OCCUPIER("I have visited this address and spoken to the main occupier"),
  I_HAVE_NOT_VISITED_THE_ADDRESS_BUT_I_HAVE_SPOKEN_TO_THE_MAIN_OCCUPIER("I have not visited the address but I have spoken to the main occupier"),
}
