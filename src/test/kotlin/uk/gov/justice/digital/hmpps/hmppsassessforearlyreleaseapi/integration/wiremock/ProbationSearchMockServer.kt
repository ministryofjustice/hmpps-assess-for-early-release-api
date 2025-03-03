package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val PROBATION_SEARCH_WIREMOCK_PORT = 8094

class ProbationSearchMockServer : WireMockServer(PROBATION_SEARCH_WIREMOCK_PORT) {
  fun stubSearchForPersonOnProbation(crn: String = "X12345") {
    stubFor(
      post(urlEqualTo("/search"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            """[
                {
                 "offenderId": 1,
                 "otherIds": { "crn": "$crn" },
                 "offenderManagers": [
                    {
                     "active": true,
                     "staff": { "code": "STAFF1"}
                    } 
                 ]
                }
               ]
            """.trimMargin(),
          ).withStatus(200),
        ),
    )
  }
}
