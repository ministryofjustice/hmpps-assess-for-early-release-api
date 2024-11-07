package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

import org.springframework.stereotype.Service

@Service
class ProbationService(
  private val deliusApiClient: DeliusApiClient,
  private val probationSearchApiClient: ProbationSearchApiClient,
) {
  fun getCurrentResponsibleOfficer(prisonNumber: String): DeliusOffenderManager? {
    val deliusRecord = probationSearchApiClient.searchForPersonOnProbation(prisonNumber)
    return if (deliusRecord != null) {
      deliusApiClient.getOffenderManager(deliusRecord.otherIds.crn)
    } else {
      null
    }
  }
}
