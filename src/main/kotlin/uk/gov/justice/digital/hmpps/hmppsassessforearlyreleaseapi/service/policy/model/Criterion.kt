package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

data class Criterion(
  val code: String,
  val name: String,
  val questions: List<Question> = emptyList(),
) {
  constructor(
    code: String,
    name: String,
    question: Question,
  ) : this(code, name, listOf(question))
}

data class Question(
  val text: String,
  val name: String,
  val hint: String? = null,
)
