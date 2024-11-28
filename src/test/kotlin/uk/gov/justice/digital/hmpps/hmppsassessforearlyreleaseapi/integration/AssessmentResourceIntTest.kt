package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ASSESS_ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.ENTER_CURFEW_ADDRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PREPARE_FOR_RELEASE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.PRINT_LICENCE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Task.REVIEW_APPLICATION_AND_SEND_FOR_DECISION
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.LOCKED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.TaskStatus.READY_TO_START
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole.PRISON_CA
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.DOES_NOT_WANT_TO_BE_TAGGED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NOWHERE_TO_STAY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.OTHER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import java.time.LocalDate

private const val PRISON_NUMBER = TestData.PRISON_NUMBER
private const val GET_CURRENT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment"
private const val OPT_OUT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-out"
private const val OPT_IN_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/opt-in"
private const val SUBMIT_ASSESSMENT_URL = "/offender/$PRISON_NUMBER/current-assessment/submit-for-address-checks"

class AssessmentResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var assessmentRepository: AssessmentRepository

  @Autowired
  private lateinit var offenderRepository: OffenderRepository

  @Nested
  inner class GetCurrentAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
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
    fun `should return the current assessment for an offender`() {
      prisonRegisterMockServer.stubGetPrisons()

      val assessment = webTestClient.get()
        .uri(GET_CURRENT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(AssessmentSummary::class.java)
        .returnResult().responseBody!!

      assertThat(assessment).isEqualTo(
        AssessmentSummary(
          forename = "FIRST-1",
          surname = "LAST-1",
          prisonNumber = PRISON_NUMBER,
          dateOfBirth = LocalDate.of(1978, 3, 20),
          hdced = LocalDate.of(2020, 10, 25),
          crd = LocalDate.of(2020, 11, 14),
          location = "Birmingham (HMP)",
          status = NOT_STARTED,
          policyVersion = "1.0",
          tasks = mapOf(
            PRISON_CA to listOf(
              TaskProgress(name = ASSESS_ELIGIBILITY, progress = READY_TO_START),
              TaskProgress(name = ENTER_CURFEW_ADDRESS, progress = LOCKED),
              TaskProgress(name = REVIEW_APPLICATION_AND_SEND_FOR_DECISION, progress = LOCKED),
              TaskProgress(name = PREPARE_FOR_RELEASE, progress = LOCKED),
              TaskProgress(name = PRINT_LICENCE, progress = LOCKED),
            ),
          ),
        ),
      )
    }
  }

  @Nested
  inner class OptOut {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .bodyValue(OptOutRequest(NO_REASON_GIVEN))
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .bodyValue(OptOutRequest(NOWHERE_TO_STAY))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(OptOutRequest(DOES_NOT_WANT_TO_BE_TAGGED))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-checks-complete.sql",
    )
    @Test
    fun `should opt-out an offender`() {
      prisonRegisterMockServer.stubGetPrisons()
      val optOutRequest = OptOutRequest(OTHER, "an opt-out reason")

      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(optOutRequest)
        .exchange()
        .expectStatus()
        .isNoContent

      val offender = offenderRepository.findByPrisonNumber(PRISON_NUMBER)
        ?: Assertions.fail("couldn't find offender with prison number: $PRISON_NUMBER")
      val assessment = assessmentRepository.findByOffender(offender)
      assertThat(assessment.first().status).isEqualTo(OPTED_OUT)
    }

    @Test
    fun `should return 401 if otherDescription not given when reasonType is other`() {
      prisonRegisterMockServer.stubGetPrisons()
      val optOutRequest = OptOutRequest(OTHER)

      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(optOutRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }
  }

  @Nested
  inner class OptIn {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-checks-complete.sql",
    )
    @Test
    fun `should opt-in an offender`() {
      prisonRegisterMockServer.stubGetPrisons()

      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(OptOutRequest(OTHER, "an opt-out reason"))
        .exchange()
        .expectStatus()
        .isNoContent

      webTestClient.put()
        .uri(OPT_IN_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isNoContent

      val offender = offenderRepository.findByPrisonNumber(PRISON_NUMBER)
        ?: Assertions.fail("couldn't find offender with prison number: $PRISON_NUMBER")
      val assessment = assessmentRepository.findByOffender(offender)
      assertThat(assessment.first().status).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `should return 401 if otherDescription not given when reasonType is other`() {
      prisonRegisterMockServer.stubGetPrisons()
      val optOutRequest = OptOutRequest(OTHER)

      webTestClient.put()
        .uri(OPT_OUT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(optOutRequest)
        .exchange()
        .expectStatus()
        .isBadRequest
    }
  }

  @Nested
  inner class SubmitAssessment {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.put()
        .uri(SUBMIT_ASSESSMENT_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.put()
        .uri(SUBMIT_ASSESSMENT_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.put()
        .uri(SUBMIT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-offender-with-checks-complete.sql",
    )
    @Test
    fun `should submit an assessment`() {
      prisonRegisterMockServer.stubGetPrisons()

      webTestClient.put()
        .uri(SUBMIT_ASSESSMENT_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isNoContent

      val updatedAssessment = assessmentRepository.findAll().first()
      assertThat(updatedAssessment).isNotNull
      assertThat(updatedAssessment.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
    }
  }

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
