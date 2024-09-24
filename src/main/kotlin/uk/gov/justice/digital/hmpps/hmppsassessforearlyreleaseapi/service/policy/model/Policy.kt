package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

data class Policy(
  val code: String,
  val suitabilityCriteria: List<Check> = emptyList(),
  val eligibilityCriteria: List<Check> = emptyList(),
)
