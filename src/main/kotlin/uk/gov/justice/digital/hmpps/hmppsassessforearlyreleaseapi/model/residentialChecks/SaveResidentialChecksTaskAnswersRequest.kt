package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

data class SaveResidentialChecksTaskAnswersRequest(
  val answers: Map<String, Any>,
  val taskCode: String,
)
