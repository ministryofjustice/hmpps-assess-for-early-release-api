package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Agent.Companion.SYSTEM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository

const val TRANSFERRED_EVENT_NAME = "assess-for-early-release.prisoner.transferred"

@Service
@Transactional
class TransferPrisonService(
  private val offenderRepository: OffenderRepository,
  private val assessmentService: AssessmentService,
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
    offenderRepository.saveAllAndFlush(listOf(updatedOffender))

    val changes = mapOf(
      "NOMS-ID" to prisonNumber,
      "PRISON-TRANSFERRED-FROM" to existingPrisonId,
      "PRISON-TRANSFERRED-TO" to prisonCode,
    )
    val assessmentEntity = assessmentService.getCurrentAssessment(prisonNumber)
    assessmentEntity.recordEvent(
      eventType = AssessmentEventType.PRISON_TRANSFERRED,
      changes,
      agent = SYSTEM_AGENT.toEntity(),
    )
    assessmentRepository.save(assessmentEntity)

    telemetryClient.trackEvent(
      TRANSFERRED_EVENT_NAME,
      changes,
      null,
    )
  }
}
