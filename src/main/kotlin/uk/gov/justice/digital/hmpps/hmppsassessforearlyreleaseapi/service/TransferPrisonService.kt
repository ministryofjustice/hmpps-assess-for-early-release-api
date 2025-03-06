package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository

const val TRANSFERRED_EVENT_NAME = "assess-for-early-release.prisoner.transferred"

@Service
@Transactional
class TransferPrisonService(
  private val offenderRepository: OffenderRepository,
  private val assessmentRepository: AssessmentRepository,
  private val telemetryClient: TelemetryClient,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferPrisoner(
    prisonNumber: String,
    prisonCode: String,
  ) {
    val existingOffender = offenderRepository.findByPrisonNumber(prisonNumber) ?: return
    val existingPrisonId = existingOffender.prisonId

    log.info("Updating prison code ${existingOffender.prisonId} to $prisonCode for prisoner number $prisonNumber")
    log.debug("Updating offender: {}", existingOffender.id)

    val updatedOffender = existingOffender.copy(prisonId = prisonCode)
    val changes = mapOf(
      "prisonNumber" to prisonNumber,
      "prisonTransferredFrom" to existingPrisonId,
      "prisonTransferredTo" to prisonCode,
    )
    val assessmentEntity = updatedOffender.currentAssessment()
    assessmentEntity.recordEvent(
      eventType = AssessmentEventType.PRISON_TRANSFERRED,
      changes,
      agent = SYSTEM_AGENT,
    )
    assessmentRepository.save(assessmentEntity)
    offenderRepository.saveAllAndFlush(listOf(updatedOffender))

    telemetryClient.trackEvent(
      TRANSFERRED_EVENT_NAME,
      changes,
      null,
    )
  }
}
