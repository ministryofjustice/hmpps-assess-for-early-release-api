package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.probation

import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService

@Service
class OffenderManagerChangedEventListener(
  private val probationService: ProbationService,
  private val mapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    const val OFFENDER_MANAGER_CHANGED = "OFFENDER_MANAGER_CHANGED"
  }

  @SqsListener("hmppsoffenderqueue", factory = "hmppsQueueContainerFactoryProxy")
  fun onProbationOffenderEvent(requestJson: String) {
    val (message, messageAttributes) = mapper.readValue(requestJson, HMPPSMessage::class.java)
    val eventType = messageAttributes.eventType.Value
    log.info("Received message $message, type $eventType")

    when (eventType) {
      OFFENDER_MANAGER_CHANGED -> {
        val (crn) = mapper.readValue(message, HMPPSReceiveProbationEvent::class.java)
        probationService.offenderManagerChanged(crn)
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

data class HMPPSReceiveProbationEvent(
  val crn: String,
)
