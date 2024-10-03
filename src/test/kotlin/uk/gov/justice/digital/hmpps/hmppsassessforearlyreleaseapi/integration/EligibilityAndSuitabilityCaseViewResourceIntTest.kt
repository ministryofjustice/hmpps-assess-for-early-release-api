package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriteriaType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import java.nio.charset.StandardCharsets

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
      prisonRegisterMockServer.stubGetPrisons()

      webTestClient.get()
        .uri(GET_ELIGIBILITY_AND_SUITABILITY_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("eligibility-and-suitability-view"), true)
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
      prisonRegisterMockServer.stubGetPrisons()

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

      webTestClient.get()
        .uri(GET_ELIGIBILITY_CRITERION_VIEW_URL("sex-offender-register"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("eligibility-criterion-view"), true)
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
      prisonRegisterMockServer.stubGetPrisons()

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

      webTestClient.get()
        .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("suitability-criterion-view"), true)
    }
  }

  @Nested
  open inner class PerformCriteriaCheck {
    val payload = CriterionCheck(
      code = "category-a",
      type = CriteriaType.SUITABILITY,
      answers = mapOf("categoryA" to false),
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
    fun `can update a criterion check`() {
      prisonRegisterMockServer.stubGetPrisons()

      run {
        val criterionView = webTestClient.get()
          .uri(GET_SUITABILITY_CRITERION_VIEW_URL("category-a"))
          .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
          .exchange()
          .expectStatus().isOk
          .expectBody(object : ParameterizedTypeReference<SuitabilityCriterionView>() {})
          .returnResult().responseBody!!

        assertThat(criterionView.criterion.status).isEqualTo(SUITABLE)
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(true)
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
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(false)
      }
    }

    @Test
    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-eligibility-checks.sql",
    )
    fun `can record a criterion check`() {
      val payload = CriterionCheck(
        code = "recalled-for-breaching-hdc-curfew",
        type = CriteriaType.ELIGIBILITY,
        answers = mapOf("recalledForBreachingHdcCurfew" to true),
      )

      prisonRegisterMockServer.stubGetPrisons()

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
        assertThat(criterionView.criterion.questions.first().answer).isEqualTo(true)
      }
    }
  }

  private fun serializedContent(name: String) =
    this.javaClass.getResourceAsStream("/test_data/responses/$name.json")!!.bufferedReader(
      StandardCharsets.UTF_8,
    ).readText()

  private companion object {
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonRegisterMockServer.stop()
    }
  }
}
