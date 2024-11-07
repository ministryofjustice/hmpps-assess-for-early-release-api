package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.typeReference

@Component
class DeliusApiClient(@Qualifier("oauthDeliusApiClient") val communityApiClient: WebClient) {

  fun getOffenderManager(crn: String): DeliusOffenderManager? {
    val communityApiResponse = communityApiClient
      .get()
      .uri("/probation-case/{crn}/responsible-community-manager", crn)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(typeReference<DeliusOffenderManager>())
      .onErrorResume {
        when {
          it is WebClientResponseException && it.statusCode == HttpStatus.NOT_FOUND -> {
            Mono.empty()
          }
          else -> Mono.error(it)
        }
      }
      .block()
    return communityApiResponse
  }
}
