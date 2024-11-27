import com.fasterxml.jackson.databind.ObjectMapper
import io.awspring.cloud.sqs.annotation.SqsListener
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.instrumentation.annotations.WithSpan
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TransferPrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService

fun interface EventProcessingCompleteHandler {
  fun complete()
}

val NO_OP = EventProcessingCompleteHandler { }

@Service
class ProbationOffenderEventListener(
  private val done: EventProcessingCompleteHandler = NO_OP,
  private val probationService: ProbationService,
  private val mapper: ObjectMapper,
  private val transferPrisonerService: TransferPrisonService,
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
