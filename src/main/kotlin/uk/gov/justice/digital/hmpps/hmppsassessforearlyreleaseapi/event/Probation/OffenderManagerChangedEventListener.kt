import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService

fun interface ProbationEventProcessingCompleteHandler {
  fun complete()
}

val NO_OP = ProbationEventProcessingCompleteHandler { }

@Service
class OffenderManagerChangedEventListener(
  private val done: ProbationEventProcessingCompleteHandler = NO_OP,
  private val probationService: ProbationService,
  private val mapper: ObjectMapper,
) {
  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    const val OFFENDER_MANAGER_CHANGED = "OFFENDER_MANAGER_CHANGED"
  }

  @SqsListener("hmppsoffenderqueue", factory = "hmppsQueueContainerFactoryProxy")
  @WithSpan(value = "hmpps-assess-for-early-release-probation-event-queue", kind = SpanKind.SERVER)
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
    done.complete()
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
