package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.enums.TelemetryEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class OffenderService(
  private val offenderRepository: OffenderRepository,
  private val prisonService: PrisonService,
  private val probationService: ProbationService,
  private val telemetryClient: TelemetryClient,
  private val assessmentService: AssessmentService,
  private val assessmentRepository: AssessmentRepository,
) {

  fun createOrUpdateOffender(prisonNumber: String) {
    val prisoners = prisonService.searchPrisonersByNomisIds(listOf(prisonNumber))
    if (prisoners.isEmpty()) {
      val msg = "Could not find prisoner with prisonNumber $prisonNumber in prisoner search"
      log.warn(msg)
      error(msg)
    }

    val prisoner = prisoners.first()
    if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      if (offender != null) {
        updateOffender(offender, prisoner)
      } else {
        createOffender(prisoner)
      }
    }
  }

  private fun createOffender(prisoner: PrisonerSearchPrisoner) {
    log.debug("Create new offender for prisoner {}", prisoner)
    val crn = probationService.getCaseReferenceNumber(prisoner.prisonerNumber)

    val offender = offenderRepository.save(
      Offender(
        prisonNumber = prisoner.prisonerNumber,
        prisonId = prisoner.prisonId!!,
        forename = prisoner.firstName,
        surname = prisoner.lastName,
        dateOfBirth = prisoner.dateOfBirth,
        crn = crn,
      ),
    )

    val assessment = assessmentService.createAssessment(
      offender,
      prisonerNumber = prisoner.prisonerNumber,
      prisoner.bookingId!!.toLong(),
      prisoner.homeDetentionCurfewEligibilityDate!!,
      prisoner.conditionalReleaseDate,
      prisoner.sentenceStartDate,
    )
    offender.assessments.add(assessment)
    val changes = mapOf(
      "prisonNumber" to prisoner.prisonerNumber,
      "homeDetentionCurfewEligibilityDate" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
    )

    telemetryClient.trackEvent(
      TelemetryEvent.PRISONER_CREATED_EVENT_NAME.key,
      changes,
      null,
    )
  }

  private fun updateOffender(offender: Offender, prisoner: PrisonerSearchPrisoner) {
    log.debug("Update offender for prisoner {}", prisoner)

    val currentAssessment = assessmentService.getCurrentAssessment(prisoner.prisonerNumber)
    if (hasOffenderBeenUpdated(offender, prisoner, currentAssessment)) {
      currentAssessment.hdced = prisoner.homeDetentionCurfewEligibilityDate!!
      currentAssessment.crd = prisoner.conditionalReleaseDate
      currentAssessment.sentenceStartDate = prisoner.sentenceStartDate
      offender.forename = prisoner.firstName
      offender.surname = prisoner.lastName
      offender.dateOfBirth = prisoner.dateOfBirth
      offender.lastUpdatedTimestamp = LocalDateTime.now()

      offenderRepository.save(offender)

      val changes = mapOf(
        "prisonNumber" to prisoner.prisonerNumber,
        "firstName" to prisoner.firstName,
        "lastName" to prisoner.lastName,
        "dateOfBirth" to prisoner.dateOfBirth.format(DateTimeFormatter.ISO_DATE),
        "homeDetentionCurfewEligibilityDate" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
      )

      currentAssessment.recordEvent(
        eventType = AssessmentEventType.PRISONER_UPDATED,
        changes,
        agent = SYSTEM_AGENT,
      )
      assessmentRepository.save(currentAssessment)

      telemetryClient.trackEvent(
        TelemetryEvent.PRISONER_UPDATED_EVENT_NAME.key,
        changes,
        null,
      )
    }
  }

  private fun hasOffenderBeenUpdated(
    offender: Offender,
    prisoner: PrisonerSearchPrisoner,
    currentAssessment: Assessment,
  ) = currentAssessment.hdced != prisoner.homeDetentionCurfewEligibilityDate ||
    currentAssessment.sentenceStartDate != prisoner.sentenceStartDate ||
    currentAssessment.crd != prisoner.conditionalReleaseDate ||
    offender.forename != prisoner.firstName ||
    offender.surname != prisoner.lastName ||
    offender.dateOfBirth != prisoner.dateOfBirth

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
