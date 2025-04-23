package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.client.mangeUsers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class ManagedUsersApiClient(@Qualifier("managedUsersApiWebClient") val managedUsersApiWebClient: WebClient) {

  fun getEmail(username: String): ManagedUserEmailResponse? {
    val managedUserEmailResponse = managedUsersApiWebClient
      .get()
      .uri("/users/$username/email")
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(ManagedUserEmailResponse::class.java)
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
