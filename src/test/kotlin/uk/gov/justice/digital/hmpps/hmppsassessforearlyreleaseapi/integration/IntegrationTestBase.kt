package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_DEFAULT_URL
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_PASSWORD
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_USERNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @MockitoSpyBean
  protected lateinit var telemetryClient: TelemetryClient

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
  ): (HttpHeaders) -> Unit = jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  protected fun jsonString(any: Any) = objectMapper.writeValueAsString(any) as String

  companion object {
    private val pgContainer = PostgresContainer.instance
    val log: Logger = LoggerFactory.getLogger(this::class.java)

    @JvmStatic
    @DynamicPropertySource
    fun properties(registry: DynamicPropertyRegistry) {
      val url = pgContainer?.let { pgContainer.jdbcUrl } ?: DB_DEFAULT_URL
      log.info("Using TestContainers?: ${pgContainer != null}, DB url: $url")
      registry.add("spring.datasource.url") { url }
      registry.add("spring.datasource.username") { DB_USERNAME }
      registry.add("spring.datasource.password") { DB_PASSWORD }
      registry.add("spring.flyway.url") { url }
      registry.add("spring.flyway.user") { DB_USERNAME }
      registry.add("spring.flyway.password") { DB_PASSWORD }
    }
  }
}
