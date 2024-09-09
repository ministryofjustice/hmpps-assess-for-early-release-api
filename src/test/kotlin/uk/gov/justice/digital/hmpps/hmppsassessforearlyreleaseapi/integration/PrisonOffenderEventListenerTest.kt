package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.fail
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import org.springframework.transaction.annotation.Transactional
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.AdditionalInformationPrisonerUpdated
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.AdditionalInformationTransfer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.DiffCategory
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.HMPPSPrisonerUpdatedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.HMPPSReceiveDomainEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.PrisonOffenderEventListener.Companion.PRISONER_RECEIVE_EVENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.PrisonOffenderEventListener.Companion.PRISONER_UPDATED_EVENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PRISONER_CREATED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PRISONER_UPDATED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TRANSFERRED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PRISONER_NUMBER = "A1234AA"
private const val OLD_PRISON_CODE = "BMI"
private const val NEW_PRISON_CODE = "MDI"

class PrisonOffenderEventListenerTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/transfer-prison.sql",
  )
  fun `prisoner received event should update offender prison`() {
    assertThat(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)?.prisonId).isEqualTo(OLD_PRISON_CODE)

    publishDomainEventMessage(
      AdditionalInformationTransfer(
        nomsNumber = PRISONER_NUMBER,
        reason = "TRANSFERRED",
        prisonId = NEW_PRISON_CODE,
      ),
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

    assertThat(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)?.prisonId).isEqualTo(NEW_PRISON_CODE)

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
      AdditionalInformationTransfer(
        nomsNumber = someNonExistentPrisonNumber,
        reason = "TRANSFERRED",
        prisonId = NEW_PRISON_CODE,
      ),
      "A prisoner has been transferred from $OLD_PRISON_CODE to $NEW_PRISON_CODE",
    )

    awaitAtMost30Secs untilAsserted {
      verify(done).complete()
    }

    verifyNoInteractions(telemetryClient)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Transactional(readOnly = true)
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  fun `Should create a new offender`() {
    val prisonerNumber = "Z1234XY"
    val hdced = LocalDate.now().plusDays(20)
    val firstName = "new first name"
    val lastName = "new last name"

    prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
      objectMapper.writeValueAsString(
        listOf(
          PrisonerSearchPrisoner(
            bookingId = "123",
            prisonerNumber = prisonerNumber,
            prisonId = "HMI",
            firstName = firstName,
            lastName = lastName,
            homeDetentionCurfewEligibilityDate = hdced,
          ),
        ),
      ),
    )

    publishDomainEventMessage(
      AdditionalInformationPrisonerUpdated(nomsNumber = prisonerNumber, listOf(DiffCategory.SENTENCE)),
    )

    awaitAtMost30Secs untilAsserted {
      verify(done).complete()
    }

    verify(telemetryClient).trackEvent(
      PRISONER_CREATED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to prisonerNumber,
        "PRISONER_HDCED" to hdced.format(DateTimeFormatter.ISO_DATE),
      ),
      null,
    )

    val createdOffender = offenderRepository.findByPrisonerNumber(prisonerNumber) ?: fail("offender not created")
    assertThat(createdOffender.firstName).isEqualTo(firstName)
    assertThat(createdOffender.lastName).isEqualTo(lastName)
    assertThat(createdOffender.hdced).isEqualTo(hdced)
    assertThat(createdOffender.assessments).hasSize(1)
    assertThat(createdOffender.assessments.iterator().next().status).isEqualTo(AssessmentStatus.NOT_STARTED)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  fun `Should update an existing offender`() {
    val newHdced = LocalDate.now().plusDays(20)
    val newCrd = LocalDate.now().plusDays(56)
    val newFirstName = "new first name"
    val newLastName = "new last name"

    prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
      objectMapper.writeValueAsString(
        listOf(
          PrisonerSearchPrisoner(
            PRISONER_NUMBER,
            firstName = newFirstName,
            lastName = newLastName,
            homeDetentionCurfewEligibilityDate = newHdced,
            conditionalReleaseDate = newCrd,
          ),
        ),
      ),
    )

    publishDomainEventMessage(
      AdditionalInformationPrisonerUpdated(nomsNumber = PRISONER_NUMBER, listOf(DiffCategory.SENTENCE)),
    )

    awaitAtMost30Secs untilAsserted {
      verify(done).complete()
    }

    verify(telemetryClient).trackEvent(
      PRISONER_UPDATED_EVENT_NAME,
      mapOf(
        "NOMS-ID" to PRISONER_NUMBER,
        "PRISONER-FIRST_NAME" to newFirstName,
        "PRISONER-LAST_NAME" to newLastName,
        "PRISONER_HDCED" to newHdced.format(DateTimeFormatter.ISO_DATE),
      ),
      null,
    )

    val updatedOffender = offenderRepository.findByPrisonerNumber(PRISONER_NUMBER) ?: fail("could not find updated offender")
    assertThat(updatedOffender.firstName).isEqualTo(newFirstName)
    assertThat(updatedOffender.lastName).isEqualTo(newLastName)
    assertThat(updatedOffender.hdced).isEqualTo(newHdced)
    assertThat(updatedOffender.crd).isEqualTo(newCrd)

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  private fun publishDomainEventMessage(
    additionalInformation: AdditionalInformationTransfer,
    description: String,
  ) {
    val jsonMessage = jsonString(
      HMPPSReceiveDomainEvent(
        eventType = PRISONER_RECEIVE_EVENT_TYPE,
        additionalInformation = additionalInformation,
        occurredAt = Instant.now().toString(),
        description = description,
        version = "1.0",
      ),
    )

    publishDomainEventMessage(jsonMessage, PRISONER_RECEIVE_EVENT_TYPE)
  }

  protected fun publishDomainEventMessage(
    additionalInformation: AdditionalInformationPrisonerUpdated,
  ) {
    val jsonMessage = jsonString(
      HMPPSPrisonerUpdatedEvent(
        eventType = PRISONER_UPDATED_EVENT_TYPE,
        additionalInformation = additionalInformation,
        occurredAt = Instant.now().toString(),
        description = "Release dates calculated for $PRISONER_NUMBER",
        version = "1.0",
      ),
    )
    publishDomainEventMessage(jsonMessage, PRISONER_UPDATED_EVENT_TYPE)
  }

  private fun publishDomainEventMessage(jsonMessage: String, eventType: String) {
    domainEventsTopicSnsClient.publish(
      PublishRequest.builder()
        .topicArn(domainEventsTopicArn)
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
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonerSearchApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonerSearchApiMockServer.stop()
    }
  }
}