package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.request

data class PrisonerSearchByPrisonerNumbersRequest(
  val prisonerNumbers: List<String>,
)
