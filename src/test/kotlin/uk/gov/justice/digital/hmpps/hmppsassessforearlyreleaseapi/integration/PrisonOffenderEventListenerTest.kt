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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.GenericChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.AdditionalInformationPrisonerUpdated
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.AdditionalInformationTransfer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.DiffCategory
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.HMPPSPrisonerUpdatedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.HMPPSReceiveDomainEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.PrisonOffenderEventListener.Companion.PRISONER_RECEIVE_EVENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.event.PrisonOffenderEventListener.Companion.PRISONER_UPDATED_EVENT_TYPE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.ProbationSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentEventRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PRISONER_CREATED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PRISONER_UPDATED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TRANSFERRED_EVENT_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private const val PRISON_NUMBER = "A1234AA"
private const val OLD_PRISON_CODE = "BMI"
private const val NEW_PRISON_CODE = "MDI"

class PrisonOffenderEventListenerTest : SqsIntegrationTestBase() {

  @Autowired
  lateinit var offenderRepository: OffenderRepository

  @Autowired
  lateinit var assessmentEventRepository: AssessmentEventRepository

  private val awaitAtMost30Secs
    get() = await.atMost(Duration.ofSeconds(30))

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/transfer-prison.sql",
  )
  fun `prisoner received event should update offender prison`() {
    // Given
    val message = AdditionalInformationTransfer(
      nomsNumber = PRISON_NUMBER,
      reason = "TRANSFERRED",
      prisonId = NEW_PRISON_CODE,
    )
    // When
    publishDomainEventMessage(message)

    // Then
    assertThat(offenderRepository.findByPrisonNumber(PRISON_NUMBER)?.prisonId).isEqualTo(OLD_PRISON_CODE)
    awaitAtMost30Secs untilAsserted {
      verify(telemetryClient).trackEvent(
        TRANSFERRED_EVENT_NAME,
        mapOf(
          "prisonNumber" to PRISON_NUMBER,
          "prisonTransferredFrom" to OLD_PRISON_CODE,
          "prisonTransferredTo" to NEW_PRISON_CODE,
        ),
        null,
      )
    }

    assertThat(offenderRepository.findByPrisonNumber(PRISON_NUMBER)?.prisonId).isEqualTo(NEW_PRISON_CODE)

    val assessment = offenderRepository.findByPrisonNumber(PRISON_NUMBER)?.currentAssessment()
    val events = assessmentEventRepository.findByAssessmentId(assessmentId = assessment?.id!!)
    assertThat(events).hasSize(1)
    val event = events.first() as GenericChangedEvent
    assertThat(event.eventType).isEqualTo(AssessmentEventType.PRISON_TRANSFERRED)
    assertThat(event.changes).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "prisonNumber" to PRISON_NUMBER,
        "prisonTransferredFrom" to OLD_PRISON_CODE,
        "prisonTransferredTo" to NEW_PRISON_CODE,
      ),
    )

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/transfer-prison.sql",
  )
  fun checkNoEventWhenNoRecordsToUpdate() {
    val someNonExistentPrisonNumber = "ZZ1234AA"
    assertThat(offenderRepository.findByPrisonNumber(someNonExistentPrisonNumber)).isNull()

    publishDomainEventMessage(
      AdditionalInformationTransfer(
        nomsNumber = someNonExistentPrisonNumber,
        reason = "TRANSFERRED",
        prisonId = NEW_PRISON_CODE,
      ),
    )

    awaitAtMost30Secs untilAsserted {
      verify(transferPrisonService).transferPrisoner(someNonExistentPrisonNumber, NEW_PRISON_CODE)
      verifyNoInteractions(telemetryClient)
    }
    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Transactional
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  fun `Should create a new offender `() {
    // Given
    val prisonNumber = "Z1234XY"
    val crn = "DX12340A"
    val hdced = LocalDate.now().plusDays(20)
    val firstName = "new first name"
    val lastName = "new last name"

    prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
      objectMapper.writeValueAsString(
        listOf(
          PrisonerSearchPrisoner(
            bookingId = "123",
            prisonerNumber = prisonNumber,
            prisonId = "HMI",
            firstName = firstName,
            lastName = lastName,
            dateOfBirth = LocalDate.of(1981, 5, 23),
            homeDetentionCurfewEligibilityDate = hdced,
            cellLocation = "A-1-002",
            mostSeriousOffence = "Robbery",
            prisonName = "Cardiff",
          ),
        ),
      ),
    )
    probationSearchApiMockServer.stubSearchForPersonOnProbation(crn)
    deliusMockServer.stubGetOffenderManager(crn)

    val message = AdditionalInformationPrisonerUpdated(nomsNumber = prisonNumber, listOf(DiffCategory.SENTENCE))

    // When
    publishDomainEventMessage(message)

    // Then
    awaitAtMost30Secs untilAsserted {
      verify(telemetryClient).trackEvent(
        PRISONER_CREATED_EVENT_NAME,
        mapOf(
          "prisonNumber" to prisonNumber,
          "homeDetentionCurfewEligibilityDate" to hdced.format(DateTimeFormatter.ISO_DATE),
        ),
        null,
      )
    }

    val createdOffender = offenderRepository.findByPrisonNumber(prisonNumber) ?: fail("offender not created")
    assertThat(createdOffender.forename).isEqualTo(firstName)
    assertThat(createdOffender.surname).isEqualTo(lastName)
    assertThat(createdOffender.hdced).isEqualTo(hdced)
    assertThat(createdOffender.assessments).hasSize(1)
    assertThat(createdOffender.crn).isEqualTo(crn)

    val assessment = createdOffender.assessments.first()
    assertThat(assessment.status).isEqualTo(AssessmentStatus.NOT_STARTED)
    assertThat(assessment.responsibleCom).isNotNull

    val events = assessmentEventRepository.findByAssessmentId(assessment.id)
    assertThat(events).hasSize(1)
    val event = events.first() as GenericChangedEvent
    assertThat(event.eventType).isEqualTo(AssessmentEventType.PRISONER_CREATED)
    assertThat(event.changes).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "prisonNumber" to prisonNumber,
        "homeDetentionCurfewEligibilityDate" to hdced.format(DateTimeFormatter.ISO_DATE),
      ),
    )

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  @Test
  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/some-offenders.sql",
  )
  fun `Should update an existing offender`() {
    // Given
    val newHdced = LocalDate.now().plusDays(20)
    val newCrd = LocalDate.now().plusDays(56)
    val newFirstName = "new first name"
    val newLastName = "new last name"
    val newDob = LocalDate.of(1994, 7, 16)

    prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
      objectMapper.writeValueAsString(
        listOf(
          PrisonerSearchPrisoner(
            PRISON_NUMBER,
            firstName = newFirstName,
            lastName = newLastName,
            dateOfBirth = newDob,
            homeDetentionCurfewEligibilityDate = newHdced,
            conditionalReleaseDate = newCrd,
            cellLocation = "A-1-002",
            mostSeriousOffence = "Robbery",
            prisonName = "Cardiff",
          ),
        ),
      ),
    )

    val message = AdditionalInformationPrisonerUpdated(nomsNumber = PRISON_NUMBER, listOf(DiffCategory.SENTENCE))

    // When
    publishDomainEventMessage(message)

    // Then
    awaitAtMost30Secs untilAsserted {
      verify(telemetryClient).trackEvent(
        PRISONER_UPDATED_EVENT_NAME,
        mapOf(
          "prisonNumber" to PRISON_NUMBER,
          "firstName" to newFirstName,
          "lastName" to newLastName,
          "dateOfBirth" to newDob.format(DateTimeFormatter.ISO_DATE),
          "homeDetentionCurfewEligibilityDate" to newHdced.format(DateTimeFormatter.ISO_DATE),
        ),
        null,
      )
    }

    val updatedOffender = offenderRepository.findByPrisonNumber(PRISON_NUMBER) ?: fail("could not find updated offender")
    assertThat(updatedOffender.forename).isEqualTo(newFirstName)
    assertThat(updatedOffender.surname).isEqualTo(newLastName)
    assertThat(updatedOffender.hdced).isEqualTo(newHdced)
    assertThat(updatedOffender.crd).isEqualTo(newCrd)

    val assessment = updatedOffender.assessments.first()
    val events = assessmentEventRepository.findByAssessmentId(assessment.id)
    assertThat(events).hasSize(1)
    val event = events.first() as GenericChangedEvent
    assertThat(event.eventType).isEqualTo(AssessmentEventType.PRISONER_UPDATED)
    assertThat(event.changes).containsExactlyInAnyOrderEntriesOf(
      mapOf(
        "prisonNumber" to PRISON_NUMBER,
        "firstName" to newFirstName,
        "lastName" to newLastName,
        "dateOfBirth" to newDob.format(DateTimeFormatter.ISO_DATE),
        "homeDetentionCurfewEligibilityDate" to newHdced.format(DateTimeFormatter.ISO_DATE),
      ),
    )

    assertThat(getNumberOfMessagesCurrentlyOnQueue()).isEqualTo(0)
  }

  private fun publishDomainEventMessage(
    additionalInformation: AdditionalInformationTransfer,
    description: String = "A prisoner has been transferred from $OLD_PRISON_CODE to $NEW_PRISON_CODE",
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
        description = "Release dates calculated for $PRISON_NUMBER",
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
    val deliusMockServer = DeliusMockServer()
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()
    val probationSearchApiMockServer = ProbationSearchMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      deliusMockServer.start()
      prisonerSearchApiMockServer.start()
      probationSearchApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      deliusMockServer.stop()
      prisonerSearchApiMockServer.stop()
      probationSearchApiMockServer.stop()
    }
  }
}
