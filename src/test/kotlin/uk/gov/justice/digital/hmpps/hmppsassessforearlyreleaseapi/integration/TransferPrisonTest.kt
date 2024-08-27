package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.AdditionalInformationTransfer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.HMPPSReceiveDomainEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.PrisonOffenderEventListener.Companion.PRISONER_RECEIVE_EVENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.Done
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TRANSFERRED_EVENT_NAME
import java.time.Duration
import java.time.Instant

private const val PRISONER_NUMBER = "A1234AA"
private const val OLD_PRISON_CODE = "BMI"
private const val NEW_PRISON_CODE = "MDI"

class TransferPrisonTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/transfer-prison.sql",
  )
  fun check() {
    assertThat(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)?.prisonId ?: null).isEqualTo(OLD_PRISON_CODE)

    publishDomainEventMessage(
      PRISONER_RECEIVE_EVENT_TYPE,
      AdditionalInformationTransfer(nomsNumber = PRISONER_NUMBER, reason = "TRANSFERRED", prisonId = NEW_PRISON_CODE),
      "A prisoner has been transferred from $OLD_PRISON_CODE to $NEW_PRISON_CODE",
    )

    awaitAtMost30Secs untilAsserted {
      verify(done).complete()
    }

    verify(telemetryClient).trackEvent(
      TRANSFERRED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to PRISONER_NUMBER,
        "PRISON-TRANSFERRED-FROM" to OLD_PRISON_CODE,
        "PRISON-TRANSFERRED-TO" to NEW_PRISON_CODE,
      ),
      null,
    )

    assertThat(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)?.prisonId ?: null).isEqualTo(NEW_PRISON_CODE)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/transfer-prison.sql",
  )
  fun checkNoEventWhenNoRecordsToUpdate() {
    val someNonExistentPrisonNumber = "ZZ1234AA"
    assertThat(offenderRepository.findByPrisonerNumber(someNonExistentPrisonNumber)).isNull()

    publishDomainEventMessage(
      PRISONER_RECEIVE_EVENT_TYPE,
      AdditionalInformationTransfer(nomsNumber = someNonExistentPrisonNumber, reason = "TRANSFERRED", prisonId = NEW_PRISON_CODE),
      "A prisoner has been transferred from $OLD_PRISON_CODE to $NEW_PRISON_CODE",
    )

    awaitAtMost30Secs untilAsserted {
      verify(done).complete()
    }

    verifyNoInteractions(telemetryClient)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  private fun publishDomainEventMessage(
    eventType: String,
    additionalInformation: AdditionalInformationTransfer,
    description: String,
  ) {
    domainEventsTopicSnsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
        .message(
          jsonString(
            HMPPSReceiveDomainEvent(
              eventType = eventType,
              additionalInformation = additionalInformation,
              occurredAt = Instant.now().toString(),
              description = description,
              version = "1.0",
            ),
          ),
        )
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
          ),
        )
        .build(),
    )
  }
}
