package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.aResponse

private const val MANAGED_USERS_WIREMOCK_PORT = 8098

class ManagedUsersMockServer : WireMockServer(MANAGED_USERS_WIREMOCK_PORT) {

  fun stubGetOffenderManager(username: String = "a-prison-user", email: String = "aled.evans@moj.gov.uk", verified: Boolean = true) {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/users/$username/email")).willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(
          """{
              "username": "$username",
              "email": "$email",
              "verified": $verified
          }""",
        ).withStatus(200),
      ),
    )
  }

  fun stubGetOffenderManager404(username: String = "a-prison-user") {
    stubFor(
      WireMock.get(WireMock.urlEqualTo("/users/$username/email"))
        .willReturn(aResponse().withStatus(404)),
    )
  }
}
