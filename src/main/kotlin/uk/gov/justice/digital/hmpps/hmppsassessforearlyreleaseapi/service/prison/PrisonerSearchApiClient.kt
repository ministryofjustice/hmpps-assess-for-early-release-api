package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.model.request.PrisonerSearchByPrisonerNumbersRequest

@Service
class PrisonerSearchApiClient(@Qualifier("oauthPrisonerSearchClient") val prisonerSearchApiWebClient: WebClient) {
  private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

  fun searchPrisonersByNomisIds(nomisIds: List<String>): List<PrisonerSearchPrisoner> {
    val requestBody = PrisonerSearchByPrisonerNumbersRequest(nomisIds)
    log.info("searchPrisonersByNomisIds request body: $requestBody")
    try {
      return prisonerSearchApiWebClient
        .post()
        .uri("/prisoner-search/prisoner-numbers")
        .accept(MediaType.APPLICATION_JSON)
        .bodyValue(requestBody)
        .retrieve()
        .bodyToMono(typeReference<List<PrisonerSearchPrisoner>>())
        .block() ?: emptyList()
    } catch (e: Exception) {
      log.info("Exception calling search prisoners by nomisIds: $e")
      throw e
    }
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}
