package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val GOVUK_WIREMOCK_PORT = 8096
class GovUkMockServer : WireMockServer(GOVUK_WIREMOCK_PORT) {

  fun stubGetBankHolidays() {
    stubFor(
      get(urlEqualTo("/bank-holidays.json"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          )
            .withBody(
              """{
                  "england-and-wales": {
                  "division": "england-and-wales",
                  "events": [
                      {
                          "title": "New Year’s Day",
                          "date": "2018-01-01",
                          "notes": "",
                          "bunting": true
                      },
                      {
                          "title": "Good Friday",
                          "date": "2018-03-30",
                          "notes": "",
                          "bunting": false
                      },
                      {
                          "title": "Easter Monday",
                          "date": "2018-04-02",
                          "notes": "",
                          "bunting": true
                      },
                      {
                          "title": "Early May bank holiday",
                          "date": "2018-05-07",
                          "notes": "",
                          "bunting": true
                      }
                    ]
                  }
                }""",
            ).withStatus(200),
        ),
    )
  }
}
