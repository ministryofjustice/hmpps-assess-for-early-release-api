package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient

@Service
class PrisonApiClient(@Qualifier("oauthPrisonClient") val prisonApiWebClient: WebClient) {
  fun getUserDetails(username: String): PrisonApiUserDetail? = prisonApiWebClient
    .get()
    .uri("/api/users/$username")
    .accept(MediaType.APPLICATION_JSON)
    .retrieve()
    .bodyToMono(PrisonApiUserDetail::class.java)
    .block()
}
