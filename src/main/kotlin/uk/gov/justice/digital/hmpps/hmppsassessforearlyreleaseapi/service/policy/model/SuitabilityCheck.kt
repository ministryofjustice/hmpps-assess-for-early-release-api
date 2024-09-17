package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

sealed interface SuitabilityCheck {
  val code: String
  val name: String

  data class YesNo(
    override val code: String,
    override val name: String,
    val question: String,
  ) : SuitabilityCheck
}
