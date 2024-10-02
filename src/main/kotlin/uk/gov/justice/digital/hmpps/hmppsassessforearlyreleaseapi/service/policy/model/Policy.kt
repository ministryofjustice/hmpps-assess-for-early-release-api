package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

data class Policy(
  val code: String,
  val suitabilityCriteria: List<Criterion> = emptyList(),
  val eligibilityCriteria: List<Criterion> = emptyList(),
)
