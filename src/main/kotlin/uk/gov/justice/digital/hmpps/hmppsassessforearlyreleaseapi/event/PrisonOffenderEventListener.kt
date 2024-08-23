package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TransferPrisonerService

@Service
class PrisonOffenderEventListener(
  private val mapper: ObjectMapper,
  private val transferPrisonerService: TransferPrisonerService,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    const val PRISONER_RECEIVE_EVENT_TYPE = "prison-offender-events.prisoner.received"
  }

  @SqsListener("domaineventsqueue", factory = "hmppsQueueContainerFactoryProxy")
  @WithSpan(value = "hmpps-assess-for-early-release-prisoner-event-queue", kind = SpanKind.SERVER)
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

      else -> {
        log.debug("Ignoring message with type $eventType")
      }
    }
  }
}

data class HMPPSReceiveDomainEvent(
  val eventType: String? = null,
  val additionalInformation: AdditionalInformationMerge,
  val version: String,
  val occurredAt: String,
  val description: String,
)

data class AdditionalInformationMerge(
  val nomsNumber: String,
  val reason: String,
  val prisonId: String,
)

data class HMPPSEventType(val Value: String, val Type: String)
data class HMPPSMessageAttributes(val eventType: HMPPSEventType)
data class HMPPSMessage(
  val Message: String,
  val MessageAttributes: HMPPSMessageAttributes,
)
