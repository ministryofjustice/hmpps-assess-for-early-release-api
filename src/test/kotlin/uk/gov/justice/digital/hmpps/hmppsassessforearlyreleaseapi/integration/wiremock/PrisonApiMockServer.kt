package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val PRISON_API_WIREMOCK_PORT = 8095

class PrisonApiMockServer : WireMockServer(PRISON_API_WIREMOCK_PORT) {
  fun stubGetUserDetails(username: String = "a-prison-user") {
    stubFor(
      get(urlEqualTo("/api/users/$username")).willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(
          """{
            "staffId": 486561,
            "username": "$username",
            "firstName": "Omu",
            "lastName": "User",
            "activeCaseLoadId": "MDI",
            "accountStatus": "ACTIVE",
            "lockedFlag": false,
            "expiredFlag": false,
            "active": true
          }
          """,
        ).withStatus(200),
      ),
    )
  }
}
