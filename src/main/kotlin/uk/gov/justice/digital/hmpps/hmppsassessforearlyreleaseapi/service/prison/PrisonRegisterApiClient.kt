package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference

@Service
class PrisonRegisterApiClient(@Qualifier("prisonRegisterClient") val prisonRegisterApiWebClient: WebClient) {
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
