package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.within
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

private const val GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL =
  "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility-and-suitability"

private val GET_ELIGIBILITY_CRITERION_VIEW_URL =
  { code: String -> "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility/$code" }

private val GET_SUITABILITY_CRITERION_VIEW_URL =
  { code: String -> "/offender/${TestData.PRISON_NUMBER}/current-assessment/suitability/$code" }

private const val PERFORM_CRITERIA_CHECK =
  "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility-and-suitability-check"

class EligibilityAndSuitabilityCaseViewResourceIntTest : SqsIntegrationTestBase() {

  private companion object {
    val prisonerSearchApiMockServer = PrisonerSearchMockServer()
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonRegisterMockServer.start()
      prisonerSearchApiMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonRegisterMockServer.stop()
      prisonerSearchApiMockServer.stop()
    }
  }

  @Nested
  inner class GetEligibilityAndSuitabilityCaseView {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    @Test
    fun `should return the eligibility criterion for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = BOOKING_ID,
              prisonerNumber = TestData.PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      webTestClient.get()
        .uri(GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("eligibility-and-suitability-view"), JsonCompareMode.STRICT)
    }
  }

  @Nested
  inner class GetEligibilityCriterionView {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("123"))
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("123"))
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("123"))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    @Test
    fun `request non existent code`() {
      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("some-invalid-code"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    @Test
    fun `should return the initial checks for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = BOOKING_ID,
              prisonerNumber = TestData.PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("sex-offender-register"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("eligibility-criterion-view"), JsonCompareMode.STRICT)
    }
  }

  @Nested
  inner class GetSuitabilityCriterionView {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("123"))
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("123"))
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("123"))
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    @Test
    fun `request non-existent code`() {
      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("some-invalid-code"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isNotFound
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    @Test
    fun `should return the suitability criterion for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = BOOKING_ID,
              prisonerNumber = TestData.PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("suitability-criterion-view"), JsonCompareMode.STRICT)
    }
  }

  @Nested
  open inner class PerformEligibilityCheckResult {
    private val payload = CriterionCheck(
      code = "category-a",
      type = CriteriaType.SUITABILITY,
      answers = mapOf("categoryA" to true),
      agent = PRISON_CA_AGENT,
    )

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(PERFORM_CRITERIA_CHECK)
        .bodyValue(payload)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(PERFORM_CRITERIA_CHECK)
        .bodyValue(payload)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(PERFORM_CRITERIA_CHECK)
        .bodyValue(payload)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    fun `can update a criterion check result`() {
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = BOOKING_ID,
              prisonerNumber = TestData.PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      run {
        val criterionView = webTestClient.get()
          .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(typeReference<SuitabilityCriterionView>())
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(SUITABLE)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(false)
      }

      webTestClient.put()
        .uri(PERFORM_CRITERIA_CHECK)
        .bodyValue(payload)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk

      run {
        val criterionView = webTestClient.get()
          .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(typeReference<SuitabilityCriterionView>())
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(UNSUITABLE)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(true)

        assertLastUpdatedByEvent(testAssessmentRepository.findAll().first())
      }
    }

    @Test
    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-unstarted-eligibility-checks.sql",
    )
    fun `can record a criterion check`() {
      prisonRegisterMockServer.stubGetPrisons()
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = BOOKING_ID,
              prisonerNumber = TestData.PRISON_NUMBER,
              prisonId = "HMI",
              firstName = "FIRST-1",
              lastName = "LAST-1",
              dateOfBirth = LocalDate.of(1981, 5, 23),
              homeDetentionCurfewEligibilityDate = LocalDate.now().plusDays(7),
              cellLocation = "A-1-002",
              mostSeriousOffence = "Robbery",
              prisonName = "Birmingham (HMP)",
            ),
          ),
        ),
      )

      val payload = CriterionCheck(
        code = "recalled-for-breaching-hdc-curfew",
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf("recalledForBreachingHdcCurfew" to false),
        agent = PRISON_CA_AGENT,
      )

      run {
        val criterionView = webTestClient.get()
          .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("recalled-for-breaching-hdc-curfew"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(typeReference<EligibilityCriterionView>())
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(NOT_STARTED)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(null)
      }

      run {
        val eligibilityAndSuitabilityView = webTestClient.put()
          .uri(PERFORM_CRITERIA_CHECK)
          .bodyValue(payload)
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(typeReference<EligibilityAndSuitabilityCaseView>())
          .returnResult().responseBody!!

        assertThat(eligibilityAndSuitabilityView.overallStatus).isEqualTo(IN_PROGRESS)
        val updatedCriteria = eligibilityAndSuitabilityView.eligibility.find { it.code == payload.code }
        assertThat(updatedCriteria).isNotNull()
        assertThat(updatedCriteria!!.status).isEqualTo(ELIGIBLE)
        assertThat(updatedCriteria.questions.first().answer).isEqualTo(false)
        assertLastUpdatedByEvent(testAssessmentRepository.findAll().first())
      }
    }
  }

  private fun assertLastUpdatedByEvent(assessment: Assessment) {
    assertThat(assessment.lastUpdateByUserEvent).isNotNull
    assessment.lastUpdateByUserEvent?.let {
      with(it) {
        assertThat(eventType).isEqualTo(AssessmentEventType.STATUS_CHANGE)
        assertThat(eventTime).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS))
        assertThat(agent.role).isEqualTo(UserRole.PRISON_CA)
        assertThat(agent.username).isEqualTo("prisonUser")
      }
    }
  }

  private fun serializedContent(name: String) = this.javaClass.getResourceAsStream("/test_data/responses/$name.json")!!.bufferedReader(
    StandardCharsets.UTF_8,
  ).readText()
}
