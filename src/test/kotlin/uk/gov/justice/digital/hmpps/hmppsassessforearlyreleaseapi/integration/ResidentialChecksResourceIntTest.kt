package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.json.JsonCompareMode
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.AddressDetailsAnswers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonerSearchMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.Agent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentialChecksTaskAnswerRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.RESIDENTIAL_CHECK_TASK_CODE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.VisitedAddress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.nio.charset.StandardCharsets
import java.time.LocalDate

private const val GET_RESIDENTIAL_CHECKS_VIEW_URL =
  "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID/residential-checks"
private const val GET_RESIDENTIAL_CHECKS_TASK_URL =
  "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID/residential-checks/tasks/$RESIDENTIAL_CHECK_TASK_CODE"
private const val SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL =
  "/offender/$PRISON_NUMBER/current-assessment/address-request/$ADDRESS_REQUEST_ID/residential-checks/answers"

class ResidentialChecksResourceIntTest : SqsIntegrationTestBase() {
  @Autowired
  private lateinit var residentialChecksTaskAnswerRepository: ResidentialChecksTaskAnswerRepository

  @Nested
  inner class GetResidentialChecksView {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-eligible-and-suitable-offender.sql",
    )
    @Test
    fun `should return the residential checks for an offender`() {
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
              prisonerNumber = PRISON_NUMBER,
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
        .uri(GET_RESIDENTIAL_CHECKS_VIEW_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("residential-checks-view"), JsonCompareMode.STRICT)
    }
  }

  @Nested
  inner class GetResidentialChecksTask {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_TASK_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_TASK_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_RESIDENTIAL_CHECKS_TASK_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/an-eligible-and-suitable-offender.sql",
    )
    @Test
    fun `should return the residential check task info for an assessment and task code`() {
      prisonerSearchApiMockServer.stubSearchPrisonersByNomisIds(
        objectMapper.writeValueAsString(
          listOf(
            PrisonerSearchPrisoner(
              bookingId = "123",
              prisonerNumber = PRISON_NUMBER,
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
        .uri(GET_RESIDENTIAL_CHECKS_TASK_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody().json(serializedContent("residential-checks-task"), JsonCompareMode.STRICT)
    }
  }

  @Nested
  inner class SaveResidentialChecksTaskAnswers {
    private val saveResidentialChecksTaskAnswersRequest =
      SaveResidentialChecksTaskAnswersRequest(
        taskCode = "address-details-and-informed-consent",
        answers = mapOf(
          "electricitySupply" to "true",
          "visitedAddress" to "I_HAVE_NOT_VISITED_THE_ADDRESS_BUT_I_HAVE_SPOKEN_TO_THE_MAIN_OCCUPIER",
          "mainOccupierConsentGiven" to "true",
        ),
        agent = Agent("user", UserRole.PROBATION_COM, "BDF329"),
      )

    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .bodyValue(saveResidentialChecksTaskAnswersRequest)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .headers(setAuthorisation())
        .bodyValue(saveResidentialChecksTaskAnswersRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .bodyValue(saveResidentialChecksTaskAnswersRequest)
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should save residential checks task answers`() {
      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(saveResidentialChecksTaskAnswersRequest)
        .exchange()
        .expectStatus()
        .isCreated

      val answers = residentialChecksTaskAnswerRepository.findAll().first()
      assertThat(answers).isNotNull
      assertThat(answers.taskCode).isEqualTo(saveResidentialChecksTaskAnswersRequest.taskCode)
      assertThat(answers.getAnswers()).isEqualTo(
        AddressDetailsAnswers(
          electricitySupply = true,
          visitedAddress = VisitedAddress.I_HAVE_NOT_VISITED_THE_ADDRESS_BUT_I_HAVE_SPOKEN_TO_THE_MAIN_OCCUPIER,
          mainOccupierConsentGiven = true,
        ),
      )
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should update existing residential checks task answers`() {
      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(saveResidentialChecksTaskAnswersRequest)
        .exchange()
        .expectStatus()
        .isCreated

      var answersEntities = residentialChecksTaskAnswerRepository.findAll()
      assertThat(answersEntities).size().isEqualTo(1)
      var taskAnswers = answersEntities.first()
      assertThat(taskAnswers).isNotNull
      assertThat(taskAnswers.taskCode).isEqualTo(saveResidentialChecksTaskAnswersRequest.taskCode)
      assertThat(taskAnswers.toAnswersMap()["electricitySupply"]).isEqualTo(true)

      val newAnswers = mapOf(
        "electricitySupply" to "false",
        "visitedAddress" to "I_HAVE_NOT_VISITED_THE_ADDRESS_BUT_I_HAVE_SPOKEN_TO_THE_MAIN_OCCUPIER",
        "mainOccupierConsentGiven" to "false",
      )
      val newSaveAnswersRequest = saveResidentialChecksTaskAnswersRequest.copy(answers = newAnswers)

      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(newSaveAnswersRequest)
        .exchange()
        .expectStatus()
        .isCreated

      answersEntities = residentialChecksTaskAnswerRepository.findAll()
      assertThat(answersEntities).size().isEqualTo(1)
      taskAnswers = answersEntities.first()
      assertThat(taskAnswers).isNotNull
      assertThat(taskAnswers.taskCode).isEqualTo(saveResidentialChecksTaskAnswersRequest.taskCode)
      assertThat(taskAnswers.toAnswersMap()["electricitySupply"]).isEqualTo(false)
    }

    @Sql(
      "classpath:test_data/reset.sql",
      "classpath:test_data/a-standard-address-check-request.sql",
    )
    @Test
    fun `should validate task answers`() {
      val invalidSaveAnswersRequestJson = """
        {
          "taskCode" : "assess-this-persons-risk",
          "answers" : {
            "pomPrisonBehaviourInformation" : "${"x".repeat(1001)}",
            "vloOfficerForCase": true,
            "informationThatCannotBeDisclosed": false
          },
          "agent": {
            "username": "user",
            "role": "PROBATION_COM",
            "onBehalfOf": "BDF329"
          }
        }
      """.trimIndent()

      webTestClient.post()
        .uri(SAVE_RESIDENTIAL_CHECKS_TASK_ANSWERS_URL)
        .header(HttpHeaders.CONTENT_TYPE, "application/json")
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .bodyValue(invalidSaveAnswersRequestJson)
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody().json(serializedContent("invalid-task-answers-response"), JsonCompareMode.STRICT)
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
