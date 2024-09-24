package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class OsPlacesApiClient(
  @Qualifier("osPlacesClient") private val osPlacesApiWebClient: WebClient,
  @Value("\${os.places.api.key}") private val apiKey: String,
) {
  // TODO  : health check
  // TODO : error handling?

  fun getAddressesForPostcode(postcode: String): List<OsPlacesApiDPA> {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/postcode?postcode=$postcode&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
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
    return searchResult?.results?.map { it.dpa }?.get(0) ?: throw IllegalArgumentException("Invalid uprn: $uprn")
//    return OsPlacesApiDPA("dsad", "a", "a", "a", "d", "dfa", ¡"dsfa", 32.3, 23.3, LocalDate.now())
  }
}
