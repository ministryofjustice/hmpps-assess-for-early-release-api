package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilAsserted
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.test.context.jdbc.Sql
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.probation.HMPPSReceiveProbationEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.probation.OffenderManagerChangedEventListener.Companion.OFFENDER_MANAGER_CHANGED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.time.Duration

private const val STAFF_CODE = "STAFF1"
private const val STAFF_USERNAME = "a-com"
private const val OLD_STAFF_EMAIL = "a-com@justice.gov.uk"
private const val NEW_STAFF_EMAIL = "staff-code-1-com@justice.gov.uk"
private const val CRN = "X123456"

@ExtendWith(OutputCaptureExtension::class)
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
    assertThat(
      staffRepository.findByStaffCodeOrUsernameIgnoreCase(
        STAFF_CODE,
        STAFF_USERNAME,
      ).first()?.email,
    ).isEqualTo(OLD_STAFF_EMAIL)

    deliusMockServer.stubGetOffenderManager(CRN, STAFF_CODE)
    deliusMockServer.stubPutAssignDeliusRole(STAFF_USERNAME.trim().uppercase())

    val event = HMPPSReceiveProbationEvent(crn = CRN)

    publishHmppsOffenderEventMessage(event)

    assertEventOffenderManagerChanged(STAFF_CODE)
    assertThat(staffRepository.findByStaffCodeOrUsernameIgnoreCase(STAFF_CODE, STAFF_USERNAME).first()?.email).isEqualTo(NEW_STAFF_EMAIL)
    assertThat(getNumberOfMessagesCurrentlyOnUpdateComQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/an-staff.sql",
  )
  fun `should create a new staff record`() {
    val newStaffCode = "STAFF2"
    assertThat(staffRepository.findByStaffCode(newStaffCode)).isNull()

    deliusMockServer.stubGetOffenderManager(CRN, newStaffCode)
    deliusMockServer.stubPutAssignDeliusRole(STAFF_USERNAME.trim().uppercase())

    val event = HMPPSReceiveProbationEvent(crn = CRN)

    publishHmppsOffenderEventMessage(event)

    assertEventOffenderManagerChanged(newStaffCode)

    assertThat(staffRepository.findByStaffCode(newStaffCode)).isNotNull()
    assertThat(getNumberOfMessagesCurrentlyOnUpdateComQueue()).isEqualTo(0)
  }

  private fun assertEventOffenderManagerChanged(staffCode: String) {
    awaitAtMost30Secs untilCallTo {
      mergeOffenderQueue.sqsClient.countMessagesOnQueue(mergeOffenderQueue.queueUrl).get()
    } matches { it == 0 }

    awaitAtMost30Secs untilAsserted {
      verify(telemetryClient).trackEvent(
        OFFENDER_MANAGER_CHANGED,
        mapOf(
          "STAFF-CODE" to staffCode,
          "USERNAME" to STAFF_USERNAME.uppercase(),
          "EMAIL" to NEW_STAFF_EMAIL,
          "FORENAME" to "Konli",
          "SURNAME" to "Gorkon",
        ),
        null,
      )
    }
  }

  private fun publishHmppsOffenderEventMessage(
    event: HMPPSReceiveProbationEvent,
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
    )
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
