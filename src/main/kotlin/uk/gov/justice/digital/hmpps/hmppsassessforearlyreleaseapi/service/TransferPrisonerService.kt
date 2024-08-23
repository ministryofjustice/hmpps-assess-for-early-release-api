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
class TransferPrisonerService(
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
    val updatedOffender = offenderRepository.findByPrisonerNumber(nomisId) ?: return done.complete()

    log.info("Transfering prisoner number $nomisId to prison code $prisonCode")
    log.debug("Updating assessment: {}", updatedOffender.id)

    updatedOffender.prisonId = prisonCode
    offenderRepository.saveAllAndFlush(listOf(updatedOffender))

    telemetryClient.trackEvent(
      TRANSFERRED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to nomisId,
        "PRISON-TRANSFERRED-TO" to prisonCode,
      ),
      null,
    )
    return done.complete()
  }
}
