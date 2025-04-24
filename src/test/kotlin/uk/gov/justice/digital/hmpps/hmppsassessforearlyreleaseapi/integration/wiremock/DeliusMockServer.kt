package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.put
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val DELIUS_WIREMOCK_PORT = 8091

class DeliusMockServer : WireMockServer(DELIUS_WIREMOCK_PORT) {

  fun stubGetOffenderManager(crn: String = "X12345", code: String = "STAFF1") {
    stubFor(
      get(urlEqualTo("/probation-case/$crn/responsible-community-manager")).willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(
          """{
            "id": 125,
            "code": "$code",
            "name": {
              "forename": "Jimmy",
              "surname": "Vivers"
            },
            "username": "a-com",
            "email": "staff-code-1-com@justice.gov.uk",
            "team": {
              "code": "team-code-1",
              "description": "staff-description-1",
              "borough": { "code": "borough-code-1", "description": "borough-description-1" },
              "district": { "code": "district-code-1", "description": "district-description-1", "borough": { "code": "borough-code-1", "description": "borough-description-1" } }
            },
            "provider": { 
              "code": "probationArea-code-1", 
              "description": "probationArea-description-1"
            }
          }""",
        ).withStatus(200),
      ),
    )
  }

  fun stubGetOffenderManager404(crn: String = "X12345") {
    stubFor(
      get(urlEqualTo("/probation-case/$crn/responsible-community-manager")).willReturn(
        aResponse().withHeader("Content-Type", "application/json").withBody(
          """{}""",
        ).withStatus(404),
      ),
    )
  }

  fun stubGetStaffDetailsByUsername(username: String = "com-user") {
    stubFor(
      get(urlEqualTo("/staff/$username"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            """
            {
              "code": "AB00001",
              "id": 2000,
              "username": "com-user",
              "email": "comuser@probation.gov.uk",
              "teams": [],
              "name": {
              "forename": "com",
              "surname": "user"
            }
            }
            """.trimIndent(),
          ).withStatus(200),
        ),
    )
  }

  fun stubPostStaffDetailsByUsernameDataNotFound() {
    stubFor(
      get(urlEqualTo("/staff"))
        .willReturn(aResponse().withStatus(404)),
    )
  }

  fun stubPutAssignDeliusRole(username: String = "A-COM") {
    stubFor(
      put(urlEqualTo("/users/${username.trim().uppercase()}/roles"))
        .willReturn(aResponse().withStatus(200)),
    )
  }

  fun stubGetStaffDetailsByStaffCode(staffCode: String) {
    stubFor(
      get(urlEqualTo("/staff/bycode/$staffCode"))
        .willReturn(
          aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            """
            {
              "code": "AB00001",
              "id": 2000,
              "username": "com-user",
              "email": "comuser@probation.gov.uk",
              "teams": [
                {
                  "code": "teamA",
                  "description": "Team A"
                },
                {
                  "code": "teamB",
                  "description": "Team B"
                }
              ],
              "name": {
                "forename": "com",
                "surname": "user"
              }
            }
            """.trimIndent(),
          ).withStatus(200),
        ),
    )
  }
}
