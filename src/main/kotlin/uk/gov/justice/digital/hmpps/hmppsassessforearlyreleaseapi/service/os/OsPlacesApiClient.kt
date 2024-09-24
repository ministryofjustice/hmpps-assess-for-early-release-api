package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.os

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDate

@Service
class OsPlacesApiClient(
  @Qualifier("osPlacesClient") private val osPlacesApiWebClient: WebClient,
  @Value("\${os.places.api.key}") private val apiKey: String,
) {
  // TODO  : health check

  fun getAddressesForPostcode(postcode: String): List<OsPlacesApiAddress> {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/postcode?postcode=$postcode&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
      .block()
    return searchResult?.results ?: emptyList()
  }

  fun getAddressForUprn(uprn: String): DPA {
    val searchResult = osPlacesApiWebClient
      .get()
      .uri("/postcode?postcode=$postcode&key=$apiKey")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(OsPlacesApiResponse::class.java)
      .block()
    return DPA("dsad", "a", "a", "a", "d", "dfa", "dsfa", 32.3, 23.3, LocalDate.now())
  }
}
