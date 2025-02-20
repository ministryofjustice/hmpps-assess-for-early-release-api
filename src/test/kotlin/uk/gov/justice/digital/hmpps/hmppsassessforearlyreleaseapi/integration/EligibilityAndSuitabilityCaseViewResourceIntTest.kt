package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Agent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.nio.charset.StandardCharsets
import java.time.LocalDate

private const val GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL =
  "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility-and-suitability"

private val GET_ELIGIBILITY_CRITERION_VIEW_URL =
  { code: String -> "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility/$code" }

private val GET_SUITABILITY_CRITERION_VIEW_URL =
  { code: String -> "/offender/${TestData.PRISON_NUMBER}/current-assessment/suitability/$code" }

private const val PERFORM_CRITERIA_CHECK =
  "/offender/${TestData.PRISON_NUMBER}/current-assessment/eligibility-and-suitability-check"

class EligibilityAndSuitabilityCaseViewResourceIntTest : SqsIntegrationTestBase() {

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
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
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
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
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
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
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
      agent = Agent("some user", UserRole.PRISON_CA, "PCD"),
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
      run {
        val criterionView = webTestClient.get()
          .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(object : ParameterizedTypeReference<SuitabilityCriterionView>() {})
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
        .isNoContent

      run {
        val criterionView = webTestClient.get()
          .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(object : ParameterizedTypeReference<SuitabilityCriterionView>() {})
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(UNSUITABLE)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(true)
      }
    }

    @Test
    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-unstarted-eligibility-checks.sql",
    )
    fun `can record a criterion check`() {
      val payload = CriterionCheck(
        code = "recalled-for-breaching-hdc-curfew",
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf("recalledForBreachingHdcCurfew" to false),
        agent = Agent("a user", UserRole.PRISON_CA, "ZJW"),
      )

      run {
        val criterionView = webTestClient.get()
          .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("recalled-for-breaching-hdc-curfew"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(object : ParameterizedTypeReference<EligibilityCriterionView>() {})
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(EligibilityStatus.NOT_STARTED)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(null)
      }

      webTestClient.put()
        .uri(PERFORM_CRITERIA_CHECK)
        .bodyValue(payload)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isNoContent

      run {
        val criterionView = webTestClient.get()
          .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("recalled-for-breaching-hdc-curfew"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(object : ParameterizedTypeReference<EligibilityCriterionView>() {})
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(ELIGIBLE)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(false)
      }
    }
  }

  private fun serializedContent(name: String) = this.javaClass.getResourceAsStream("/test_data/responses/$name.json")!!.bufferedReader(
    StandardCharsets.UTF_8,
  ).readText()

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
