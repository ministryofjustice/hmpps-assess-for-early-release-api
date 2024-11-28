package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import HMPPSReceiveProbationEvent
import OffenderManagerChangedEventListener.Companion.OFFENDER_MANAGER_CHANGED
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import java.time.Duration

private const val STAFF_IDENTIFIER = 2000L
private const val STAFF_USERNAME = "a-com"
private const val OLD_STAFF_EMAIL = "a-com@justice.gov.uk"

class OffenderManagerChangedEventListenerTest : SqsIntegrationTestBase() {

  @Autowired

  lateinit var staffRepository: StaffRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/an-staff.sql",
  )
  fun `probation received event should update staff`() {
    assertThat(staffRepository.findByStaffIdentifierOrUsernameIgnoreCase(
      STAFF_IDENTIFIER,
      STAFF_USERNAME,
    ).first()?.email).isEqualTo(OLD_STAFF_EMAIL)

    deliusMockServer.stubGetOffenderManager()

    val crn = "X123456"
    val event = HMPPSReceiveProbationEvent(crn = crn)

    publishHmppsOffenderEventMessage(event)

    awaitAtMost30Secs untilAsserted {
      verify(hmppsOffenderDone).complete()
    }

    verify(telemetryClient).trackEvent(
      OFFENDER_MANAGER_CHANGED,
      mapOf(
        "crn" to crn,
      ),
      null,
    )
  }

  private fun publishHmppsOffenderEventMessage(
    event: HMPPSReceiveProbationEvent
  ) {
    val jsonMessage = jsonString(event)
    publishHmppsOffenderEventMessage(jsonMessage, OFFENDER_MANAGER_CHANGED)
  }

  private fun publishHmppsOffenderEventMessage(jsonMessage: String, eventType: String) {
    hmppsOffenderTopicSnsClient.publish(
      PublishRequest.builder()
        .topicArn(hmppsOffenderTopicArn)
        .message(jsonMessage)
        .messageAttributes(
          mapOf(
            "eventType" to MessageAttributeValue.builder().dataType("String").stringValue(eventType)
              .build(),
          ),
        )
        .build(),
    ).get()
  }

  private companion object {
    val deliusMockServer = DeliusMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      deliusMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      deliusMockServer.stop()
    }
  }
}