package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val PRISON_REGISTER_WIREMOCK_PORT = 8092

class PrisonRegisterMockServer : WireMockServer(PRISON_REGISTER_WIREMOCK_PORT) {
  fun stubGetPrisons() {
    val json = """[
      {
        "prisonId": "AKI",
        "prisonName": "Acklington (HMP)"
      },
      {
        "prisonId": "AWI",
        "prisonName": "Ashwell (HMP)"
      },
      {
        "prisonId": "BMI",
        "prisonName": "Birmingham (HMP)"
      },
      {
        "prisonId": "BTI",
        "prisonName": "Blakenhurst (HMP)"
      },
      {
        "prisonId": "HBI",
        "prisonName": "Hollesley Bay (HMP & YOI)"
      }
    ]
    """.trimIndent()

    stubFor(
      get(urlEqualTo("/prisons"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            json,
          ).withStatus(200),
        ),
    )
  }

  fun stubGetPrisonsNoResults() {
    val json = """[]""".trimIndent()

    stubFor(
      get(urlEqualTo("/prisons"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            json,
          ).withStatus(200),
        ),
    )
  }
}
