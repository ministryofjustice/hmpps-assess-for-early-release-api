package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.junit.jupiter.api.Test
import org.springframework.test.context.jdbc.Sql
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID

private const val DELETE_ADDRESS_CHECK_REQUEST_URL = "/offender/${TestData.PRISON_NUMBER}/current-assessment/address-request/$ADDRESS_REQUEST_ID"

class AgentNotFoundTest : SqsIntegrationTestBase() {

  @Sql(
    "classpath:test_data/reset.sql",
    "classpath:test_data/a-standard-address-check-request.sql",
  )
  @Test
  fun `should delete an address check request`() {
    webTestClient.delete()
      .uri(DELETE_ADDRESS_CHECK_REQUEST_URL)
      .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
      .exchange()
      .expectBody()
      .jsonPath("$.userMessage").isEqualTo("Unexpected error: Agent is missing from the request headers")
  }
}
