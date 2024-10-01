package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PrisonRegisterApiClient(@Qualifier("prisonRegisterClient") val prisonRegisterApiWebClient: WebClient) {
  private inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}

  @Cacheable("prisons")
  fun getPrisons(): List<Prison> {
    return prisonRegisterApiWebClient
      .get()
      .uri("/prisons")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(typeReference<List<Prison>>())
      .block() ?: emptyList()
  }
}
