package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model

data class Policy(
  val code: String,
  val suitabilityCriteria: List<SuitabilityCheck> = emptyList(),
  val eligibilityCriteria: List<EligibilityCheck> = emptyList(),
)
