package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class OsPlacesApiClient(
  @Qualifier("osPlacesClient") private val osPlacesApiWebClient: WebClient,
  @Value("\${os.places.api.key}") private val apiKey: String,
) {
  fun searchForAddresses(searchQuery: String): List<OsPlacesApiDPA> {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/find?query=$searchQuery&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
      .onErrorResume { e -> if (e is WebClientResponseException && e.statusCode == HttpStatus.BAD_REQUEST) Mono.empty() else Mono.error(e) }
      .block()
    return searchResult?.results?.map { it.dpa } ?: emptyList()
  }

  fun getAddressesForPostcode(postcode: String): List<OsPlacesApiDPA> {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/postcode?postcode=$postcode&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
      .onErrorResume { e -> if (e is WebClientResponseException && e.statusCode == HttpStatus.BAD_REQUEST) Mono.empty() else Mono.error(e) }
      .block()
    return searchResult?.results?.map { it.dpa } ?: emptyList()
  }

  fun getAddressForUprn(uprn: String): OsPlacesApiDPA {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/uprn?uprn=$uprn&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
      .block()
    return searchResult?.results?.map { it.dpa }?.get(0) ?: error("Could not find an address with uprn: $uprn")
  }
}
