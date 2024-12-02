package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.probation.OffenderManagerChangedEventListener.Companion.OFFENDER_MANAGER_CHANGED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.UpdateCom
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import java.time.LocalDateTime

@Service
class StaffService(
  private val staffRepository: StaffRepository,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  /**
   * Check if record already exists. If so, check if any of the details have changed before performing an update.
   * Check if the username and staffId do not match. This should not happen, unless a Delius account is updated to point
   * at another linked account using the staffId. In this scenario, we should update the existing record to reflect
   * the new username and or staffId.
   */
  @Transactional
  fun updateComDetails(comDetails: UpdateCom) {
    val comResult = staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(
      comDetails.staffIdentifier,
      comDetails.staffUsername,
    )

    if (comResult.isNullOrEmpty()) {
      staffRepository.saveAndFlush(
        CommunityOffenderManager(
          username = comDetails.staffUsername.uppercase(),
          staffIdentifier = comDetails.staffIdentifier,
          email = comDetails.staffEmail,
          forename = comDetails.forename,
          surname = comDetails.surname,
        ),
      )

      telemetryClient.trackEvent(
        OFFENDER_MANAGER_CHANGED,
        mapOf(
          "STAFF-IDENTIFIER" to comDetails.staffIdentifier.toString(),
          "USERNAME" to comDetails.staffUsername.uppercase(),
          "EMAIL" to comDetails.staffEmail,
          "FORENAME" to comDetails.forename,
          "SURNAME" to comDetails.surname,
        ),
        null,
      )
    } else {
      if (comResult.count() > 1) {
        log.warn(
          "More then one COM record found for staffId {} username {}",
          comDetails.staffIdentifier,
          comDetails.staffUsername,
        )
      }

      val com = comResult.first() as CommunityOffenderManager

      // only update entity if data is different
      if (com.isUpdate(comDetails)) {
        staffRepository.saveAndFlush(
          com.copy(
            staffIdentifier = comDetails.staffIdentifier,
            username = comDetails.staffUsername.uppercase(),
            email = comDetails.staffEmail,
            forename = comDetails.forename,
            surname = comDetails.surname,
            lastUpdatedTimestamp = LocalDateTime.now(),
          ),
        )

        telemetryClient.trackEvent(
          OFFENDER_MANAGER_CHANGED,
          mapOf(
            "STAFF-IDENTIFIER" to comDetails.staffIdentifier.toString(),
            "USERNAME" to comDetails.staffUsername.uppercase(),
            "EMAIL" to comDetails.staffEmail,
            "FORENAME" to comDetails.forename,
            "SURNAME" to comDetails.surname,
          ),
          null,
        )
      }
    }
  }

  private fun CommunityOffenderManager.isUpdate(comDetails: UpdateCom) =
    (comDetails.forename != this.forename) ||
      (comDetails.surname != this.surname) ||
      (comDetails.staffEmail != this.email) ||
      (!comDetails.staffUsername.equals(this.username, ignoreCase = true)) ||
      (comDetails.staffIdentifier != this.staffIdentifier)
}
