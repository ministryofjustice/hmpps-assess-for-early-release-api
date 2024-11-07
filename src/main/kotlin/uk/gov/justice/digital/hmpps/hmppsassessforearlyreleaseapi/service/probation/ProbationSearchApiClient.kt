package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference

@Component
class ProbationSearchApiClient(@Qualifier("oauthProbationSearchApiClient") val probationSearchApiClient: WebClient) {

  fun searchForPersonOnProbation(
    nomisId: String,
  ): OffenderDetail? {
    try {
      val probationOffenderSearchResponse = probationSearchApiClient
        .post()
        .uri("/search")
        .bodyValue(OffenderSearchRequest(nomsNumber = nomisId))
        .accept(MediaType.APPLICATION_JSON)
        .retrieve()
        .bodyToMono(typeReference<List<OffenderDetail>>())
        .block()
      return if (probationOffenderSearchResponse?.isNotEmpty() == true) probationOffenderSearchResponse[0] else null
    } catch (e: WebClientResponseException) {
      println(e)
      throw e
    }
  }
}
