package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private val GOTENBERG_WIREMOCK_PORT = 3001
private val GOTENBERG_WIREMOCK_URL = "/forms/chromium/convert/html"

class GotenbergMockServer : WireMockServer(GOTENBERG_WIREMOCK_PORT) {
  fun stubPostPdf(pdfResponse: ByteArray) {
    stubFor(
      post(urlEqualTo(GOTENBERG_WIREMOCK_URL))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/pdf",
          ).withBody(
            pdfResponse,
          ).withStatus(200),
        ),
    )
  }

  fun stubPostPdfBadRequest() {
    stubFor(
      post(urlEqualTo(GOTENBERG_WIREMOCK_URL))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withStatus(500),
        ),
    )
  }
}
