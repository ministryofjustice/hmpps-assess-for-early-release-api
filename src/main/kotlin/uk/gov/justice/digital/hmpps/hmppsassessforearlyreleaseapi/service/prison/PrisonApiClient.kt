package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class PrisonApiClient(@Qualifier("oauthPrisonClient") val prisonApiWebClient: WebClient) {

  fun getUserDetails(username: String): PrisonApiUserDetail? {
    val managedUserEmailResponse = prisonApiWebClient
      .get()
      .uri("/api/users/$username")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(PrisonApiUserDetail::class.java)
      .onErrorResume {
        when {
          it is WebClientResponseException && it.statusCode == HttpStatus.NOT_FOUND -> {
            Mono.empty()
          }
          else -> Mono.error(it)
        }
      }
      .block()
    return managedUserEmailResponse
  }
}
