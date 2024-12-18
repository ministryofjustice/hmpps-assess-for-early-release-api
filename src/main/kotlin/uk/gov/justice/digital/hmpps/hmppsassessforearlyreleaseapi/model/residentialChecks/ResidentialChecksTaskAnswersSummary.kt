package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks

data class ResidentialChecksTaskAnswersSummary(
  val answersId: Long,
  val addressCheckRequestId: Long,
  val answers: Map<String, Any>,
  val taskCode: String,
  val taskVersion: String,
)
