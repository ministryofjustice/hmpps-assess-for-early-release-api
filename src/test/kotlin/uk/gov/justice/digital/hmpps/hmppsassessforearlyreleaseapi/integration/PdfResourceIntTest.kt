package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.GotenbergMockServer

private const val TITLE = "title"
private const val MESSAGE = "MESSAGE"
private const val GET_PDF_URL = "/pdf?title=$TITLE&message=$MESSAGE"

class PdfResourceIntTest : SqsIntegrationTestBase() {

  @Nested
  inner class GetStaffDetailsByUsername {
    @Test
    fun `should return unauthorized if no token`() {
      webTestClient.get()
        .uri(GET_PDF_URL)
        .exchange()
        .expectStatus()
        .isUnauthorized
    }

    @Test
    fun `should return forbidden if no role`() {
      webTestClient.get()
        .uri(GET_PDF_URL)
        .headers(setAuthorisation())
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return forbidden if wrong role`() {
      webTestClient.get()
        .uri(GET_PDF_URL)
        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
        .exchange()
        .expectStatus()
        .isForbidden
    }

    @Test
    fun `should return pdf`() {
      val pdfBytes = "PDF_BYTES".toByteArray()
      gotenbergMockServer.stubPostPdf(pdfBytes)

      val result = webTestClient.get()
        .uri(GET_PDF_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ByteArray::class.java)
        .returnResult().responseBody!!

      assertThat(result).isEqualTo(pdfBytes)
    }
  }

  private companion object {
    private val gotenbergMockServer = GotenbergMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      gotenbergMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      gotenbergMockServer.stop()
    }
  }
}
