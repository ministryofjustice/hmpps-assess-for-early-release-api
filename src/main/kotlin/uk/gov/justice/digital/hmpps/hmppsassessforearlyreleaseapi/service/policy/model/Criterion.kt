package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

fun interface EvaluationStrategy {
  fun isMet(answers: Map<String, Boolean>): Boolean

  companion object {
    val MET_IF_ALL_ARE_FALSE = EvaluationStrategy { answers -> answers.values.all { !it } }
    val MET_IF_ANY_ARE_FALSE = EvaluationStrategy { answers -> answers.values.any { !it } }
  }
}

data class Criterion(
  val code: String,
  val name: String,
  val questions: List<Question> = emptyList(),
  val evaluationStrategy: EvaluationStrategy = EvaluationStrategy.MET_IF_ALL_ARE_FALSE,
) : EvaluationStrategy by evaluationStrategy {
  constructor(
    code: String,
    name: String,
    question: Question,
    evaluationStrategy: EvaluationStrategy = EvaluationStrategy.MET_IF_ALL_ARE_FALSE,
  ) : this(code, name, listOf(question), evaluationStrategy)
}

data class Question(
  val text: String,
  val name: String,
  val hint: String? = null,
)
