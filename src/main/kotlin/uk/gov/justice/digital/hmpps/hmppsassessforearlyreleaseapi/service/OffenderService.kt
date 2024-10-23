package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.persistence.EntityNotFoundException
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.OffenderStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

const val PRISONER_CREATED_EVENT_NAME = "assess-for-early-release.prisoner.created"
const val PRISONER_UPDATED_EVENT_NAME = "assess-for-early-release.prisoner.updated"

@Service
class OffenderService(
  private val assessmentLifecycleService: AssessmentLifecycleService,
  private val assessmentRepository: AssessmentRepository,
  private val assessmentService: AssessmentService,
  private val offenderRepository: OffenderRepository,
  private val prisonRegisterService: PrisonRegisterService,
  private val prisonerSearchService: PrisonerSearchService,
  private val telemetryClient: TelemetryClient,
) {
  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummary> {
    val offenders = offenderRepository.findByPrisonIdAndStatus(prisonCode, OffenderStatus.NOT_STARTED)
    return offenders.map {
      OffenderSummary(it.prisonNumber, it.bookingId, it.forename, it.surname, it.hdced)
    }
  }

  @Transactional
  fun getCurrentAssessment(prisonNumber: String): AssessmentSummary {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val prisonName = prisonRegisterService.getNameForId(offender.prisonId)
    val currentAssessment = offender.currentAssessment()
    return AssessmentSummary(
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prisonName,
      status = currentAssessment.status,
      policyVersion = currentAssessment.policyVersion,
      tasks = currentAssessment.status.tasks().map { TaskProgress(it.task, it.status(currentAssessment)) },
    )
  }

  @Transactional
  fun optOut(prisonNumber: String) {
    val offender = offenderRepository.findByPrisonNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")
    val optedOutAssessment =
      offender.currentAssessment().copy(status = AssessmentStatus.OPTED_OUT, lastUpdatedTimestamp = LocalDateTime.now())
    assessmentRepository.save(optedOutAssessment)
  }

  @Transactional
  fun submitCurrentAssessment(prisonNumber: String) {
    val assessmentWithEligibilityProgress = assessmentService.getCurrentAssessment(prisonNumber)
    val newStatus = assessmentLifecycleService.submitAssessment(assessmentWithEligibilityProgress)
    val assessmentEntity = assessmentWithEligibilityProgress.assessmentEntity

    assessmentEntity.changeStatus(newStatus)
    assessmentRepository.save(assessmentEntity)
  }

  fun createOrUpdateOffender(nomisId: String) {
    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(listOf(nomisId))
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
    val offender = Offender(
      bookingId = prisoner.bookingId!!.toLong(),
      prisonNumber = prisoner.prisonerNumber,
      prisonId = prisoner.prisonId!!,
      forename = prisoner.firstName,
      surname = prisoner.lastName,
      dateOfBirth = prisoner.dateOfBirth,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
      crd = prisoner.conditionalReleaseDate,
    )
    offender.assessments.add(Assessment(offender = offender, policyVersion = PolicyService.CURRENT_POLICY_VERSION.code))
    offenderRepository.save(offender)

    telemetryClient.trackEvent(
      PRISONER_CREATED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to prisoner.prisonerNumber,
        "PRISONER_HDCED" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
      ),
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
        lastUpdatedTimestamp = LocalDateTime.now(),
      )
      offenderRepository.save(updatedOffender)
      telemetryClient.trackEvent(
        PRISONER_UPDATED_EVENT_NAME,
        mapOf(
          "NOMS-ID" to prisoner.prisonerNumber,
          "PRISONER-FIRST_NAME" to prisoner.firstName,
          "PRISONER-LAST_NAME" to prisoner.lastName,
          "PRISONER_DOB" to prisoner.dateOfBirth.format(DateTimeFormatter.ISO_DATE),
          "PRISONER_HDCED" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
        ),
        null,
      )
    }
  }

  private fun hasOffenderBeenUpdated(offender: Offender, prisoner: PrisonerSearchPrisoner) =
    offender.hdced != prisoner.homeDetentionCurfewEligibilityDate ||
      offender.crd != prisoner.conditionalReleaseDate ||
      offender.forename != prisoner.firstName ||
      offender.surname != prisoner.lastName ||
      offender.dateOfBirth != prisoner.dateOfBirth

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
