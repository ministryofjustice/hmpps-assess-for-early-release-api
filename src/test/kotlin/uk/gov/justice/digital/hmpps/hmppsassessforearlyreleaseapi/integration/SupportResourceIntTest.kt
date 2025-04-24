package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.GenericChangedEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.ProbationSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.enums.TelemetryEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.function.Consumer

private const val PRISON_NUMBER = TestData.PRISON_NUMBER
private const val CRN = "DX12340A"
private const val ASSESSMENT_ID = 1L

private const val DELETE_CURRENT_ASSESSMENT_URL = "/support/offender/$PRISON_NUMBER/assessment/current"
private const val DELETE_ASSESSMENT_URL = "/support/offender/assessment/$ASSESSMENT_ID"
private const val SEARCH_FOR_OFFENDERS = "/support/offender/search/"
private const val GET_OFFENDER = "/support/offender/$PRISON_NUMBER"
private const val GET_ASSESSMENT = "/support/offender/assessment/$ASSESSMENT_ID"
private const val GET_ASSESSMENTS = "/support/offender/$PRISON_NUMBER/assessments"

class SupportResourceIntTest : SqsIntegrationTestBase() {

  private companion object {
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()
    val probationSearchApiMockServer = ProbationSearchMockServer()
    val deliusMockServer = DeliusMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonerSearchApiMockServer.start()
      prisonRegisterMockServer.start()
      probationSearchApiMockServer.start()
      deliusMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonerSearchApiMockServer.stop()
      prisonRegisterMockServer.stop()
      probationSearchApiMockServer.stop()
      deliusMockServer.stop()
    }
  }

  @Nested
  inner class SearchForOffenders {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(SEARCH_FOR_OFFENDERS + PRISON_NUMBER)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(SEARCH_FOR_OFFENDERS + PRISON_NUMBER)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(SEARCH_FOR_OFFENDERS + PRISON_NUMBER)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should find offender when prison number given`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = SEARCH_FOR_OFFENDERS + PRISON_NUMBER
      val offender = testOffenderRepository.findByPrisonNumber(PRISON_NUMBER)!!
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val offenders = response.expectBody(typeReference<List<OffenderSearchResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(1)
      val offenderSearchResponse = offenders.first()

      assertThat(offenderSearchResponse.prisonNumber).isEqualTo(offender.prisonNumber)
      assertThat(offenderSearchResponse.prisonId).isEqualTo(offender.prisonId)
      assertThat(offenderSearchResponse.crn).isEqualTo(offender.crn)
      assertThat(offenderSearchResponse.surname).isEqualTo(offender.surname)
      assertThat(offenderSearchResponse.forename).isEqualTo(offender.forename)
      assertThat(offenderSearchResponse.dateOfBirth).isEqualTo(offender.dateOfBirth)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should find offender when crn given`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = SEARCH_FOR_OFFENDERS + CRN
      val offender = testOffenderRepository.findByCrn(CRN)!!
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val offenders = response.expectBody(typeReference<List<OffenderSearchResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(1)
      val offenderSearchResponse = offenders.first()

      assertThat(offenderSearchResponse.prisonNumber).isEqualTo(offender.prisonNumber)
      assertThat(offenderSearchResponse.prisonId).isEqualTo(offender.prisonId)
      assertThat(offenderSearchResponse.crn).isEqualTo(offender.crn)
      assertThat(offenderSearchResponse.surname).isEqualTo(offender.surname)
      assertThat(offenderSearchResponse.forename).isEqualTo(offender.forename)
      assertThat(offenderSearchResponse.dateOfBirth).isEqualTo(offender.dateOfBirth)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should find offenders that have a prison number that starts with given partial prison number`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val partialPrisonNumber = PRISON_NUMBER.substring(0, 4)
      val url = SEARCH_FOR_OFFENDERS + partialPrisonNumber

      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val offenders = response.expectBody(typeReference<List<OffenderSearchResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(4)
      assertThat(offenders.map { it.prisonNumber }).allSatisfy(
        Consumer {
          assertThat(it).startsWith(partialPrisonNumber)
        },
      )
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should find offenders that have a prison number that starts with given partial crn`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val partialCrn = CRN.substring(0, 4)
      val url = SEARCH_FOR_OFFENDERS + partialCrn

      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val offenders = response.expectBody(typeReference<List<OffenderSearchResponse>>())
        .returnResult().responseBody!!
      assertThat(offenders).hasSize(6)
      assertThat(offenders.map { it.crn }).allSatisfy(
        Consumer {
          assertThat(it).startsWith(partialCrn)
        },
      )
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should throw an exception if search string has length less than 4`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val partialPrisonNumber = PRISON_NUMBER.substring(0, 3)
      val url = SEARCH_FOR_OFFENDERS + partialPrisonNumber

      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isBadRequest
      val errorResponse = response.expectBody(ErrorResponse::class.java).returnResult().responseBody
      assertThat(errorResponse?.status).isEqualTo(HttpStatus.BAD_REQUEST.value())
      assertThat(errorResponse?.userMessage).isEqualTo("Validation failure: 400 BAD_REQUEST \"Validation failure\"")
      assertThat(errorResponse?.developerMessage).isEqualTo("400 BAD_REQUEST \"Validation failure\"")
    }
  }

  @Nested
  inner class GetOffender {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_OFFENDER)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_OFFENDER)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_OFFENDER)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should get offender when prison number given`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = GET_OFFENDER
      val offender = testOffenderRepository.findByPrisonNumber(PRISON_NUMBER)!!
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val offenderResponse = response.expectBody(typeReference<OffenderResponse>())
        .returnResult().responseBody!!

      assertThat(offenderResponse.prisonNumber).isEqualTo(offender.prisonNumber)
      assertThat(offenderResponse.prisonId).isEqualTo(offender.prisonId)
      assertThat(offenderResponse.forename).isEqualTo(offender.forename)
      assertThat(offenderResponse.surname).isEqualTo(offender.surname)
      assertThat(offenderResponse.dateOfBirth).isEqualTo(offender.dateOfBirth)
      assertThat(offenderResponse.crd).isEqualTo(offender.crd)
      assertThat(offenderResponse.crn).isEqualTo(offender.crn)
      assertThat(offenderResponse.sentenceStartDate).isEqualTo(offender.sentenceStartDate)
      assertThat(offenderResponse.createdTimestamp).isCloseTo(offender.createdTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(offenderResponse.lastUpdatedTimestamp).isCloseTo(offender.lastUpdatedTimestamp, within(1, ChronoUnit.SECONDS))
    }
  }

  @Nested
  inner class GetAssessments {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ASSESSMENTS)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ASSESSMENTS)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ASSESSMENTS)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/offenders_with_assessments.sql",
    )
    @Test
    fun `should get assessments when prison number given`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = GET_ASSESSMENTS
      val assessments = testAssessmentRepository.findByOffenderPrisonNumber(PRISON_NUMBER)
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val assessmentSearchResponseList = response.expectBody(typeReference<List<AssessmentSearchResponse>>())
        .returnResult().responseBody!!
      assertThat(assessmentSearchResponseList).hasSize(2)
      val assessment1 = assessmentSearchResponseList.first()
      assertThat(assessment1.id).isEqualTo(assessments[0].id)
      assertThat(assessment1.bookingId).isEqualTo(assessments[0].bookingId)
      assertThat(assessment1.status).isEqualTo(assessments[0].status)
      assertThat(assessment1.previousStatus).isEqualTo(assessments[0].previousStatus)
      assertThat(assessment1.createdTimestamp).isCloseTo(assessments[0].createdTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessment1.lastUpdatedTimestamp).isCloseTo(assessments[0].lastUpdatedTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessment1.deletedTimestamp).isCloseTo(assessments[0].deletedTimestamp, within(1, ChronoUnit.SECONDS))

      val assessment2 = assessmentSearchResponseList.last()
      assertThat(assessment2.id).isEqualTo(assessments[1].id)
      assertThat(assessment2.bookingId).isEqualTo(assessments[1].bookingId)
      assertThat(assessment2.status).isEqualTo(assessments[1].status)
      assertThat(assessment2.previousStatus).isEqualTo(assessments[1].previousStatus)
      assertThat(assessment2.createdTimestamp).isCloseTo(assessments[1].createdTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessment2.lastUpdatedTimestamp).isCloseTo(assessments[1].lastUpdatedTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessment2.deletedTimestamp).isNull()
    }
  }

  @Nested
  inner class GetAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ASSESSMENT)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ASSESSMENT)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ASSESSMENT)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/offenders_with_assessments.sql",
    )
    @Test
    fun `should get assessment when prison number given`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = GET_ASSESSMENT
      val assessment = testAssessmentRepository.findById(1).get()
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)

      // When
      val response = webTestClient.get()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      response.expectStatus().isOk
      val assessmentResponse = response.expectBody(typeReference<AssessmentResponse>())
        .returnResult().responseBody!!
      assertThat(assessmentResponse.id).isEqualTo(assessment.id)
      assertThat(assessmentResponse.bookingId).isEqualTo(assessment.bookingId)
      assertThat(assessmentResponse.status).isEqualTo(assessment.status)
      assertThat(assessmentResponse.previousStatus).isEqualTo(assessment.previousStatus)
      assertThat(assessmentResponse.createdTimestamp).isCloseTo(assessment.createdTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessmentResponse.lastUpdatedTimestamp).isCloseTo(assessment.lastUpdatedTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessmentResponse.deletedTimestamp).isCloseTo(assessment.deletedTimestamp, within(1, ChronoUnit.SECONDS))
      assertThat(assessmentResponse.policyVersion).isEqualTo(assessment.policyVersion)
      assertThat(assessmentResponse.addressChecksComplete).isEqualTo(assessment.addressChecksComplete)
      assertThat(assessmentResponse.responsibleCom).isNotNull
      assessmentResponse.responsibleCom?.let {
        assertThat(it.surname).isEqualTo(assessment.responsibleCom?.surname)
        assertThat(it.forename).isEqualTo(assessment.responsibleCom?.forename)
        assertThat(it.username).isEqualTo(assessment.responsibleCom?.username)
        assertThat(it.email).isEqualTo(assessment.responsibleCom?.email)
        assertThat(it.staffCode).isEqualTo(assessment.responsibleCom?.staffCode)
      }
      assertThat(assessmentResponse.team).isEqualTo(assessment.teamCode)
      assertThat(assessmentResponse.postponementDate).isEqualTo(assessment.postponementDate)
      assertThat(assessmentResponse.optOutReasonType).isEqualTo(assessment.optOutReasonType)
      assertThat(assessmentResponse.optOutReasonOther).isEqualTo(assessment.optOutReasonOther)
    }
  }

  @Nested
  inner class DeleteCurrentAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.delete()
        .uri(DELETE_CURRENT_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.delete()
        .uri(DELETE_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.delete()
        .uri(DELETE_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should soft delete current assigment for an offender`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = DELETE_CURRENT_ASSESSMENT_URL
      val initialAssessment = testAssessmentRepository.findByOffenderPrisonNumber(PRISON_NUMBER).first()
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)
      val crn = "DX12340A"

      probationSearchApiMockServer.stubSearchForPersonOnProbation(crn)
      deliusMockServer.stubGetOffenderManager(crn)

      // When
      val result = webTestClient.delete()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      assertDelete(result, initialAssessment)
    }
  }

  @Nested
  inner class DeleteAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.delete()
        .uri(DELETE_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.delete()
        .uri(DELETE_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.delete()
        .uri(DELETE_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/some-offenders.sql",
    )
    @Test
    fun `should soft delete assigment for an offender`() {
      // Given
      val roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")
      val url = DELETE_ASSESSMENT_URL
      val initialAssessment = testAssessmentRepository.findByOffenderPrisonNumber(PRISON_NUMBER).first()
      val authorisation = setAuthorisation(roles = roles, agent = TestData.PRISON_CA_AGENT)
      val crn = "DX12340A"

      probationSearchApiMockServer.stubSearchForPersonOnProbation(crn)
      deliusMockServer.stubGetOffenderManager(crn)

      // When
      val result = webTestClient.delete()
        .uri(url)
        .headers(authorisation)
        .exchange()

      // Then
      assertDelete(result, initialAssessment)
    }
  }

  private fun assertDelete(
    result: WebTestClient.ResponseSpec,
    initialAssessment: Assessment,
  ) {
    result.expectStatus().isNoContent

    val deletedAssessment = assessmentRepository.findById(initialAssessment.id).get()
    assertThat(deletedAssessment.deletedTimestamp).isNotNull()
    assertThat(deletedAssessment.deletedTimestamp)
      .isCloseTo(LocalDateTime.now(), within(2, ChronoUnit.SECONDS))

    val lastEvent = deletedAssessment.assessmentEvents.last()
    assertThat(lastEvent.eventType).isEqualTo(AssessmentEventType.ASSESSMENT_DELETED)
    assertThat(lastEvent.agent.username).isEqualTo(TestData.PRISON_CA_AGENT.username)
    assertThat(lastEvent).isOfAnyClassIn(GenericChangedEvent::class.java)
    val lastEventGeneric = lastEvent as GenericChangedEvent
    assertThat(lastEventGeneric.changes).isEqualTo(
      mapOf(
        "prisonerNumber" to "A1234AA",
      ),
    )

    verify(telemetryClient).trackEvent(
      TelemetryEvent.ASSESSMENT_DELETE_EVENT_NAME.key,
      mapOf(
        "prisonerNumber" to "A1234AA",
        "agent" to "prisonUser",
        "agentRole" to "PRISON_CA",
        "id" to initialAssessment.id.toString(),
      ),
      null,
    )

    val assessments = deletedAssessment.offender.assessments
    assertThat(assessments).hasSize(2)
    assertThat(assessments.first().id).isEqualTo(initialAssessment.id)
    assertThat(assessments.last().deletedTimestamp).isNull()
    assertThat(assessments.last().status).isEqualTo(AssessmentStatus.NOT_STARTED)
  }
}
