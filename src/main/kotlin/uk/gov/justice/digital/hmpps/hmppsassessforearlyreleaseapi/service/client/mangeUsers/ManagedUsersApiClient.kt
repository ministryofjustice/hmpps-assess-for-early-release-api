package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.client.mangeUsers

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class ManagedUsersApiClient(@Qualifier("managedUsersApiWebClient") val managedUsersApiWebClient: WebClient) {
  fun getEmail(username: String): ManagedUserEmailResponse? = managedUsersApiWebClient
    .get()
    .uri("/users/$username/email")
    .accept(MediaType.APPLICATION_JSON)
    .retrieve()
    .bodyToMono(ManagedUserEmailResponse::class.java)
    .block()
}
