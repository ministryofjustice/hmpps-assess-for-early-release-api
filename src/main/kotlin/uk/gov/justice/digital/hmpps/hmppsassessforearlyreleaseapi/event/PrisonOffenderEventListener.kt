package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.OffenderService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TransferPrisonService

@Service
class PrisonOffenderEventListener(
  private val offenderService: OffenderService,
  private val mapper: ObjectMapper,
  private val transferPrisonerService: TransferPrisonService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    const val PRISONER_RECEIVE_EVENT_TYPE = "prison-offender-events.prisoner.received"
    const val PRISONER_UPDATED_EVENT_TYPE = "prisoner-offender-search.prisoner.updated"
    val DIFF_CATEGORIES_TO_PROCESS = listOf(DiffCategory.PERSONAL_DETAILS, DiffCategory.SENTENCE)
  }

  @SqsListener("domaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onPrisonOffenderEvent(requestJson: String) {
    val (message, messageAttributes) = mapper.readValue(requestJson, HMPPSMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    when (eventType) {
      PRISONER_RECEIVE_EVENT_TYPE -> {
        val receiveEvent = mapper.readValue(message, HMPPSReceiveDomainEvent::class.java)
        if (receiveEvent.additionalInformation.reason == "TRANSFERRED") {
          val nomisId = receiveEvent.additionalInformation.nomsNumber
          val prisonCode = receiveEvent.additionalInformation.prisonId
          transferPrisonerService.transferPrisoner(nomisId, prisonCode)
        }
      }

      PRISONER_UPDATED_EVENT_TYPE -> {
        val updatedEvent = mapper.readValue(message, HMPPSPrisonerUpdatedEvent::class.java)
        if (updatedEvent.additionalInformation.categoriesChanged.any { it in DIFF_CATEGORIES_TO_PROCESS }) {
          offenderService.createOrUpdateOffender(updatedEvent.additionalInformation.nomsNumber)
        }
      }
      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }
}

data class HMPPSEventType(val Value: String, val Type: String)

data class HMPPSMessageAttributes(val eventType: HMPPSEventType)

data class HMPPSMessage(
  val Message: String,
  val MessageAttributes: HMPPSMessageAttributes,
)

data class HMPPSReceiveDomainEvent(
  val eventType: String? = null,
  val additionalInformation: AdditionalInformationTransfer,
  val version: String,
  val occurredAt: String,
  val description: String,
)

data class AdditionalInformationTransfer(
  val nomsNumber: String,
  val reason: String,
  val prisonId: String,
)

enum class DiffCategory {
  IDENTIFIERS,
  PERSONAL_DETAILS,
  ALERTS,
  STATUS,
  LOCATION,
  SENTENCE,
  RESTRICTED_PATIENT,
  INCENTIVE_LEVEL,
  PHYSICAL_DETAILS,
  CONTACT_DETAILS,
}

data class AdditionalInformationPrisonerUpdated(
  val nomsNumber: String,
  val categoriesChanged: List<DiffCategory>,
)

data class HMPPSPrisonerUpdatedEvent(
  val eventType: String? = null,
  val additionalInformation: AdditionalInformationPrisonerUpdated,
  val version: String,
  val occurredAt: String,
  val description: String,
)
