package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.model.request

data class PrisonerSearchByPrisonerNumbersRequest(
  val prisonerNumbers: List<String>,
)
