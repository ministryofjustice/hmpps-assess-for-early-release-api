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
  private val offenderRepository: OffenderRepository,
  private val prisonRegisterService: PrisonRegisterService,
  private val prisonerSearchService: PrisonerSearchService,
  private val telemetryClient: TelemetryClient,
) {
  @Transactional
  fun getCaseAdminCaseload(prisonCode: String): List<OffenderSummary> {
    val offenders = offenderRepository.findByPrisonIdAndStatus(prisonCode, OffenderStatus.NOT_STARTED)
    return offenders.map {
      OffenderSummary(it.prisonerNumber, it.bookingId, it.firstName, it.lastName, it.hdced)
    }
  }

  @Transactional
  fun getCurrentAssessment(prisonNumber: String): AssessmentSummary {
    val offender = offenderRepository.findByPrisonerNumber(prisonNumber)
      ?: throw EntityNotFoundException("Cannot find offender with prisonNumber $prisonNumber")

    val prisonIdsToNames = prisonRegisterService.getPrisonIdsAndNames()
    val offenderLocation = prisonIdsToNames[offender.prisonId] ?: throw EntityNotFoundException("Cannot find a prison with prison id in prison register: ${offender.prisonId}")

    val currentAssessment = offender.assessments.first { it.status == AssessmentStatus.NOT_STARTED }
    return AssessmentSummary(
      forename = offender.firstName,
      surname = offender.lastName,
      prisonNumber = offender.prisonerNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = offenderLocation,
      status = currentAssessment.status,
    )
  }

  fun createOrUpdateOffender(nomisId: String) {
    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(listOf(nomisId))
    if (prisoners.isEmpty()) {
      val msg = "Could not find prisoner with prisonerNumber $nomisId in prisoner search"
      log.warn(msg)
      throw Exception(msg)
    }

    val prisoner = prisoners.first()
    if (prisoner.homeDetentionCurfewEligibilityDate != null) {
      val offender = offenderRepository.findByPrisonerNumber(nomisId)
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
      prisonerNumber = prisoner.prisonerNumber,
      prisonId = prisoner.prisonId!!,
      firstName = prisoner.firstName,
      lastName = prisoner.lastName,
      hdced = prisoner.homeDetentionCurfewEligibilityDate!!,
      crd = prisoner.conditionalReleaseDate,
    )
    offender.assessments.add(Assessment(offender = offender))
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
        firstName = prisoner.firstName,
        lastName = prisoner.lastName,
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
          "PRISONER_HDCED" to prisoner.homeDetentionCurfewEligibilityDate.format(DateTimeFormatter.ISO_DATE),
        ),
        null,
      )
    }
  }

  private fun hasOffenderBeenUpdated(offender: Offender, prisoner: PrisonerSearchPrisoner) =
    offender.hdced != prisoner.homeDetentionCurfewEligibilityDate || offender.crd != prisoner.conditionalReleaseDate || offender.firstName != prisoner.firstName || offender.lastName != prisoner.lastName

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
