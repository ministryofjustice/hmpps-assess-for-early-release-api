package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase

private const val TITLE = "title"
private const val MESSAGE = "MESSAGE"
private const val GET_PDF_URL = "/pdf?title=$TITLE&message=$MESSAGE"

class PdfResourceIntTest : SqsIntegrationTestBase() {

  @Autowired
  override lateinit var webTestClient: WebTestClient

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
      mockWebServer.enqueue(MockResponse().setBody("PDF_BYTES").addHeader("Content-Type", "application/pdf"))

      val result = webTestClient.get()
        .uri(GET_PDF_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus().isOk
        .expectBody(ByteArray::class.java)
        .returnResult().responseBody!!

      assertThat(result).isEqualTo(pdfBytes)
    }
  }

  private companion object {
    private lateinit var mockWebServer: MockWebServer

    @BeforeAll
    @JvmStatic
    fun setUp() {
      mockWebServer = MockWebServer()
      mockWebServer.start(3002)
    }

    @AfterAll
    @JvmStatic
    fun tearDown() {
      mockWebServer.shutdown()
    }
  }
}
