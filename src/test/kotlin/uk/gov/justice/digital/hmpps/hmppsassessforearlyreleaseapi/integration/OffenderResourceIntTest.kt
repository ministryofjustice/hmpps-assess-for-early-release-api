package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.core.ParameterizedTypeReference
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OffenderSummary

const val PRISON_CODE = "BMI"
const val DECISION_MAKER_CASELOAD_URL = "/prison/$PRISON_CODE/decision-maker/caseload"

class OffenderResourceIntTest : SqsIntegrationTestBase() {

  @Test
  fun `should return unauthorized if no token`() {
    webTestClient.get()
      .uri(DECISION_MAKER_CASELOAD_URL)
      .exchange()
      .expectStatus()
      .isUnauthorized
  }

  @Test
  fun `should return forbidden if no role`() {
    webTestClient.get()
      .uri(DECISION_MAKER_CASELOAD_URL)
      .headers(setAuthorisation())
      .exchange()
      .expectStatus()
      .isForbidden
  }

  @Test
  fun `should return forbidden if wrong role`() {
    webTestClient.get()
      .uri(DECISION_MAKER_CASELOAD_URL)
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
  fun `should return offenders at prison with a status of not started`() {
    val offenders = webTestClient.get()
      .uri(DECISION_MAKER_CASELOAD_URL)
      .headers(setAuthorisation(roles = listOf("AFER_ADMIN")))
      .exchange()
      .expectStatus()
      .isOk
      .expectBody(object : ParameterizedTypeReference<List<OffenderSummary>>() {})
      .returnResult().responseBody!!

    assertThat(offenders).hasSize(4)
    val prisonerNumbers = offenders.map { it.prisonerNumber }
    assertThat(prisonerNumbers).containsExactlyInAnyOrder("A1234AA", "A1234AB", "A1234AD", "C1234CD")
  }
}
