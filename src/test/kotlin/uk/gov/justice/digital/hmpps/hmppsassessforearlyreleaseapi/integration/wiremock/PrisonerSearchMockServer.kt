package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import java.time.LocalDate

private const val PRISONER_SEARCH_WIREMOCK_PORT = 8093

class PrisonerSearchMockServer : WireMockServer(PRISONER_SEARCH_WIREMOCK_PORT) {
  fun stubSearchPrisonersByNomisIds(prisonerSearchResponse: String? = null) {
    val json = prisonerSearchResponse ?: """[
            {
              "prisonerNumber": "A1234AA",
              "bookingId": "123",
              "status": "ACTIVE",
              "mostSeriousOffence": "Robbery",
              "licenceExpiryDate": "${LocalDate.now().plusYears(1)}",
              "topupSupervisionExpiryDate": "${LocalDate.now().plusYears(1)}",
              "homeDetentionCurfewEligibilityDate": null,
              "releaseDate": "${LocalDate.now().plusDays(1)}",
              "confirmedReleaseDate": "${LocalDate.now().plusDays(1)}",
              "conditionalReleaseDate": "${LocalDate.now().plusDays(1)}",
              "paroleEligibilityDate": null,
              "actualParoleDate" : null,
              "postRecallReleaseDate": null,
              "legalStatus": "SENTENCED",
              "indeterminateSentence": false,
              "recall": false,
              "prisonId": "ABC",
              "bookNumber": "12345A",
              "firstName": "Test1",
              "lastName": "Person1",
              "dateOfBirth": "1985-01-01"
           },
           {
              "prisonerNumber": "A1234AB",
              "bookingId": "456",
              "status": "ACTIVE",
              "mostSeriousOffence": "Robbery",
              "licenceExpiryDate": "${LocalDate.now().plusYears(1)}",
              "topupSupervisionExpiryDate": "${LocalDate.now().plusYears(1)}",
              "homeDetentionCurfewEligibilityDate": null,
              "releaseDate": null,
              "confirmedReleaseDate": null,
              "conditionalReleaseDate": "${LocalDate.now().plusDays(1)}",
              "paroleEligibilityDate": null,
              "actualParoleDate" : null,
              "postRecallReleaseDate": null,
              "legalStatus": "SENTENCED",
              "indeterminateSentence": false,
              "recall": false,
              "prisonId": "DEF",
              "bookNumber": "67890B",
              "firstName": "Test2",
              "lastName": "Person2",
              "dateOfBirth": "1986-01-01"
           },
           {
              "prisonerNumber": "A1234AC",
              "bookingId": "789",
              "status": "INACTIVE",
              "mostSeriousOffence": "Robbery",
              "licenceExpiryDate": null,
              "topupSupervisionExpiryDate": null,
              "homeDetentionCurfewEligibilityDate": null,
              "releaseDate": null,
              "confirmedReleaseDate": null,
              "conditionalReleaseDate": null,
              "paroleEligibilityDate": null,
              "actualParoleDate" : null,
              "postRecallReleaseDate": null,
              "legalStatus": "SENTENCED",
              "indeterminateSentence": false,
              "recall": false,
              "prisonId": "GHI",
              "bookNumber": "12345C",
              "firstName": "Test3",
              "lastName": "Person3",
              "dateOfBirth": "1987-01-01"
           },
           {
              "prisonerNumber": "A1234AD",
              "bookingId": "123",
              "status": "ACTIVE",
              "mostSeriousOffence": "Robbery",
              "licenceExpiryDate": "${LocalDate.now().plusYears(1)}",
              "topUpSupervisionExpiryDate": "${LocalDate.now().plusYears(1)}",
              "homeDetentionCurfewEligibilityDate": null,
              "releaseDate": "${LocalDate.now().plusDays(1)}",
              "confirmedReleaseDate": "${LocalDate.now().plusDays(1)}",
              "conditionalReleaseDate": "${LocalDate.now().plusDays(1)}",
              "paroleEligibilityDate": null,
              "actualParoleDate" : null,
              "postRecallReleaseDate": null,
              "legalStatus": "SENTENCED",
              "indeterminateSentence": false,
              "recall": false,
              "prisonId": "GHI",
              "bookNumber": "12345C",
              "firstName": "Test3",
              "lastName": "Person3",
              "dateOfBirth": "1987-01-01"
           },
           {
              "prisonerNumber": "A1234AE",
              "bookingId": "123",
              "status": "INACTIVE",
              "mostSeriousOffence": "Robbery",
              "licenceExpiryDate": "${LocalDate.now().minusYears(1)}",
              "topUpSupervisionExpiryDate": "${LocalDate.now().plusYears(1)}",
              "homeDetentionCurfewEligibilityDate": null,
              "releaseDate": "${LocalDate.now().minusYears(1)}",
              "confirmedReleaseDate": "${LocalDate.now().plusDays(1)}",
              "conditionalReleaseDate": "${LocalDate.now().plusDays(1)}",
              "paroleEligibilityDate": null,
              "actualParoleDate" : null,
              "postRecallReleaseDate": null,
              "legalStatus": "SENTENCED",
              "indeterminateSentence": false,
              "recall": false,
              "prisonId": "GHI",
              "bookNumber": "12345C",
              "firstName": "Test3",
              "lastName": "Person3",
              "dateOfBirth": "1987-01-01"
           }
          ]
    """.trimIndent()

    stubFor(
      post(urlEqualTo("/api/prisoner-search/prisoner-numbers"))
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
