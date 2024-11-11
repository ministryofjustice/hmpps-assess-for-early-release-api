package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

import jakarta.persistence.EntityNotFoundException
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

  fun getStaffDetailsByUsername(username: String): User? {
    val staffDetails = deliusApiClient.getStaffDetailsByUsername(username)
    return staffDetails ?: throw EntityNotFoundException("Cannot find staff with username $username")
  }
}
