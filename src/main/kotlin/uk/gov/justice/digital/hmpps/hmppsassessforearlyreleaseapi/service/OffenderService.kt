package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.DeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val PRISONER_CREATED_EVENT_NAME = "assess-for-early-release.prisoner.created"
const val PRISONER_UPDATED_EVENT_NAME = "assess-for-early-release.prisoner.updated"

@Service
class OffenderService(
  private val assessmentRepository: AssessmentRepository,
  private val offenderRepository: OffenderRepository,
  private val prisonService: PrisonService,
  private val probationService: ProbationService,
  private val staffRepository: StaffRepository,
  private val telemetryClient: TelemetryClient,
) {
  fun createOrUpdateOffender(nomisId: String) {
    val prisoners = prisonService.searchPrisonersByNomisIds(listOf(nomisId))
    if (prisoners.isEmpty()) {
      val msg = "Could not find prisoner with prisonNumber $nomisId in prisoner search"
      log.warn(msg)
      error(msg)
    }

    val prisoner = prisoners.first()
    if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      val offender = offenderRepository.findByPrisonNumber(nomisId)
      if (offender != null) {
        updateOffender(offender, prisoner)
      } else {
        createOffender(prisoner)
      }
    }
  }

  private fun createOffender(prisoner: PrisonerSearchPrisoner) {
    val crn = probationService.getCaseReferenceNumber(prisoner.prisonerNumber)

    val offender = Offender(
      bookingId = prisoner.bookingId!!.toLong(),
      prisonNumber = prisoner.prisonerNumber,
      prisonId = prisoner.prisonId!!,
      forename = prisoner.firstName,
      surname = prisoner.lastName,
      dateOfBirth = prisoner.dateOfBirth,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
      crd = prisoner.conditionalReleaseDate,
      crn = crn,
      sentenceStartDate = prisoner.sentenceStartDate,
    )

    val deliusOffenderManager = crn?.let {
      probationService.getCurrentResponsibleOfficer(crn)
    }

    val communityOffenderManager = crn?.let {
      deliusOffenderManager?.let {
        staffRepository.findByStaffCode(it.code) ?: createCommunityOffenderManager(it)
      }
    }

    val assessment = Assessment(
      offender = offender,
      policyVersion = PolicyService.CURRENT_POLICY_VERSION.code,
      responsibleCom = communityOffenderManager,
      team = deliusOffenderManager?.team?.code,
    )

    offender.assessments.add(assessment)

    val changes = mapOf(
      "prisonNumber" to prisoner.prisonerNumber,
      "homeDetentionCurfewEligibilityDate" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
    )

    assessment.recordEvent(
      eventType = AssessmentEventType.PRISONER_CREATED,
      changes,
      agent = SYSTEM_AGENT,
    )
    offenderRepository.save(offender)

    telemetryClient.trackEvent(
      PRISONER_CREATED_EVENT_NAME,
      changes,
      null,
    )
  }

  private fun updateOffender(offender: Offender, prisoner: PrisonerSearchPrisoner) {
    if (hasOffenderBeenUpdated(offender, prisoner)) {
      val updatedOffender = offender.copy(
        forename = prisoner.firstName,
        surname = prisoner.lastName,
        dateOfBirth = prisoner.dateOfBirth,
        hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
        crd = prisoner.conditionalReleaseDate,
        sentenceStartDate = prisoner.sentenceStartDate,
        lastUpdatedTimestamp = LocalDateTime.now(),
      )
      offenderRepository.save(updatedOffender)

      val changes = mapOf(
        "prisonNumber" to prisoner.prisonerNumber,
        "firstName" to prisoner.firstName,
        "lastName" to prisoner.lastName,
        "dateOfBirth" to prisoner.dateOfBirth.format(DateTimeFormatter.ISO_DATE),
        "homeDetentionCurfewEligibilityDate" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
      )

      val assessmentEntity = updatedOffender.currentAssessment()
      assessmentEntity.recordEvent(
        eventType = AssessmentEventType.PRISONER_UPDATED,
        changes,
        agent = SYSTEM_AGENT,
      )
      assessmentRepository.save(assessmentEntity)

      telemetryClient.trackEvent(
        PRISONER_UPDATED_EVENT_NAME,
        changes,
        null,
      )
    }
  }

  private fun hasOffenderBeenUpdated(offender: Offender, prisoner: PrisonerSearchPrisoner) = offender.hdced != prisoner.homeDetentionCurfewEligibilityDate ||
    offender.sentenceStartDate != prisoner.sentenceStartDate ||
    offender.crd != prisoner.conditionalReleaseDate ||
    offender.forename != prisoner.firstName ||
    offender.surname != prisoner.lastName ||
    offender.dateOfBirth != prisoner.dateOfBirth

  private fun createCommunityOffenderManager(offenderManager: DeliusOffenderManager): CommunityOffenderManager = staffRepository.save(
    CommunityOffenderManager(
      staffCode = offenderManager.code,
      username = offenderManager.username,
      email = offenderManager.email,
      forename = offenderManager.name.forename,
      surname = offenderManager.name.surname,
    ),
  )

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
