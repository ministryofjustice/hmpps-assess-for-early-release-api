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

  fun getStaffDetailsByUsername(username: String): User? {
    val communityApiResponse = communityApiClient
      .get()
      .uri("/staff/{username}", username)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(typeReference<User>())
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

  fun getStaffDetailsByStaffCode(staffCode: String): User? {
    val communityApiResponse = communityApiClient
      .get()
      .uri("/staff/bycode/{code}", staffCode)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(typeReference<User>())
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

  fun assignDeliusRole(username: String) {
    communityApiClient
      .put()
      .uri("/users/{username}/roles", username)
      .accept(MediaType.APPLICATION_JSON)
      .retrieve()
      .bodyToMono(Void::class.java)
      .block()
  }
}
