package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

sealed interface EligibilityCheck {
  val code: String
  val name: String

  data class YesNo(
    override val code: String,
    override val name: String,
    val question: String,
  ) : EligibilityCheck
}
