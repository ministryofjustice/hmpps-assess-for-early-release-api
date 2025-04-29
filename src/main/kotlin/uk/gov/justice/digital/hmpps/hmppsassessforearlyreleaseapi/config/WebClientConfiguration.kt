package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.hmpps.kotlin.auth.healthWebClient
import java.time.Duration

private const val HMPPS_AUTH = "hmpps-auth"

@Configuration
class WebClientConfiguration(
  @Value("\${api.health-timeout:2s}") val healthTimeout: Duration,
  @Value("\${hmpps.auth.url}") val hmppsAuthBaseUri: String,
  @Value("\${hmpps.delius.api.url}") private val deliusApiUrl: String,
  @Value("\${hmpps.prison.api.url}") private val prisonApiUrl: String,
  @Value("\${hmpps.prisonregister.api.url}") private val prisonRegisterApiUrl: String,
  @Value("\${hmpps.prisonersearch.api.url}") private val prisonerSearchApiUrl: String,
  @Value("\${hmpps.probationsearch.api.url}") private val probationSearchApiUrl: String,
  @Value("\${os.places.api.url}") private val osPlacesApiUrl: String,
  @Value("\${gotenberg.api.url}") private val gotenbergHost: String,
  @Value("\${hmpps.govuk.api.url}") private val govUkApiUrl: String,
  @Value("\${hmpps.manageusers.api.url}") private val manageUsersApiUrl: String,
) {
  @Bean
  fun hmppsAuthHealthWebClient(builder: WebClient.Builder): WebClient = builder.healthWebClient(hmppsAuthBaseUri, healthTimeout)

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun oauthPrisonerSearchClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = getOAuthClient(authorizedClientManager)
    return getWebClient(prisonerSearchApiUrl, oauth2Client)
  }

  @Bean
  fun oauthPrisonClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = getOAuthClient(authorizedClientManager)
    return getWebClient(prisonApiUrl, oauth2Client)
  }

  @Bean
  fun oauthDeliusApiClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = getOAuthClient(authorizedClientManager)
    return getWebClient(deliusApiUrl, oauth2Client)
  }

  @Bean
  fun oauthProbationSearchApiClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = getOAuthClient(authorizedClientManager)
    return getWebClient(probationSearchApiUrl, oauth2Client)
  }

  @Bean
  fun managedUsersApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = getOAuthClient(authorizedClientManager)
    return getWebClient(manageUsersApiUrl, oauth2Client)
  }

  @Bean
  fun osPlacesClient(): WebClient = getWebClient(osPlacesApiUrl)

  @Bean
  fun gotenbergClient(): WebClient = getWebClient(gotenbergHost)

  @Bean
  fun prisonRegisterClient(): WebClient = getWebClient(prisonRegisterApiUrl)

  @Bean
  fun govUkWebClient(): WebClient = WebClient.builder().baseUrl(govUkApiUrl).build()

  private fun getOAuthClient(authorizedClientManager: OAuth2AuthorizedClientManager): ServletOAuth2AuthorizedClientExchangeFilterFunction {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId(HMPPS_AUTH)
    return oauth2Client
  }

  private fun getWebClient(url: String, oauth2Client: ServletOAuth2AuthorizedClientExchangeFilterFunction): WebClient = WebClient.builder()
    .baseUrl(url)
    .apply(oauth2Client.oauth2Configuration())
    .exchangeStrategies(
      ExchangeStrategies.builder()
        .codecs { configurer ->
          configurer.defaultCodecs()
            .maxInMemorySize(-1)
        }
        .build(),
    ).build()

  private fun getWebClient(url: String): WebClient = WebClient.builder()
    .baseUrl(url)
    .exchangeStrategies(
      ExchangeStrategies.builder()
        .codecs { configurer ->
          configurer.defaultCodecs()
            .maxInMemorySize(-1)
        }
        .build(),
    ).build()
}
