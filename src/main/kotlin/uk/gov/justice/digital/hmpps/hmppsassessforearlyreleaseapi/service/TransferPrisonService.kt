package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import jakarta.transaction.Transactional
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository

const val TRANSFERRED_EVENT_NAME = "assess-for-early-release.prisoner.transferred"

fun interface Done {
  fun complete()
}

val NO_OP = Done { }

@Service
@Transactional
class TransferPrisonService(
  private val offenderRepository: OffenderRepository,
  private val telemetryClient: TelemetryClient,
  private val done: Done = NO_OP,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun transferPrisoner(
    nomisId: String,
    prisonCode: String,
  ) {
    val existingOffender = offenderRepository.findByPrisonerNumber(nomisId) ?: return done.complete()

    log.info("Updating prison code ${existingOffender.prisonId} to $prisonCode for prisoner number $nomisId")
    log.debug("Updating offender: {}", existingOffender.id)

    val updatedOffender = existingOffender.copy(prisonId = prisonCode)
    offenderRepository.saveAllAndFlush(listOf(updatedOffender))

    telemetryClient.trackEvent(
      TRANSFERRED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to nomisId,
        "PRISON-TRANSFERRED-FROM" to existingOffender.prisonId,
        "PRISON-TRANSFERRED-TO" to prisonCode,
      ),
      null,
    )
    return done.complete()
  }
}
