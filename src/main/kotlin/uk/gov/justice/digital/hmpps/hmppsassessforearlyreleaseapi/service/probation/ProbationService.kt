package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCom
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.StaffService

@Service
class ProbationService(
  private val assessmentService: AssessmentService,
  private val deliusApiClient: DeliusApiClient,
  private val probationSearchApiClient: ProbationSearchApiClient,
  private val staffService: StaffService,
) {

  fun getCaseReferenceNumber(prisonNumber: String): String? {
    val deliusRecord = probationSearchApiClient.searchForPersonOnProbation(prisonNumber)
    return deliusRecord?.otherIds?.crn
  }

  fun getCurrentResponsibleOfficer(caseReferenceNumber: String): DeliusOffenderManager? = deliusApiClient.getOffenderManager(caseReferenceNumber)

  fun getStaffDetailsByUsername(username: String): User? {
    val staffDetails = deliusApiClient.getStaffDetailsByUsername(username)
    return staffDetails ?: throw ItemNotFoundException("Cannot find staff with username $username")
  }

  fun offenderManagerChanged(crn: String) {
    val newCom = deliusApiClient.getOffenderManager(crn)
    log.info("responsible officer code for crn $crn is ${newCom?.code}")

    if (newCom != null) {
      // If the COM does not have a username, they are assumed to be ineligible for use of this service. (e.g. the "unallocated" staff members)
      if (newCom.username != null) {
        // Assign the com role to the user if they do not have it already
        deliusApiClient.assignDeliusRole(newCom.username.trim().uppercase())

        staffService.updateComDetails(
          UpdateCom(
            staffCode = newCom.code,
            staffUsername = newCom.username,
            staffEmail = newCom.email,
            forename = newCom.name.forename,
            surname = newCom.name.surname,
          ),
        )

        assessmentService.updateTeamForResponsibleCom(newCom.code, newCom.team.code)
      }
    } else {
      log.info("newCom not found for crn: $crn")
    }
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
