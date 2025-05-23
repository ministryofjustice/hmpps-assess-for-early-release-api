package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.microsoft.applicationinsights.TelemetryClient
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cache.CacheManager
import org.springframework.http.HttpHeaders
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import org.springframework.test.context.jdbc.SqlConfig
import org.springframework.test.web.reactive.server.WebTestClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_DEFAULT_URL
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_PASSWORD
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.PostgresContainer.DB_USERNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.TestAssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers.TestOffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.HmppsAuthApiExtension
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.HmppsAuthApiExtension.Companion.hmppsAuth
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AddressDeletionEventRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentEventRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TransferPrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.gotenberg.GotenbergApiClient
import uk.gov.justice.hmpps.test.kotlin.auth.JwtAuthorisationHelper

@ExtendWith(HmppsAuthApiExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = ["spring.jpa.properties.hibernate.enable_lazy_load_no_trans=true"])
@ActiveProfiles("test")
@SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
abstract class IntegrationTestBase {

  @Autowired
  protected lateinit var webTestClient: WebTestClient

  @Autowired
  protected lateinit var testAssessmentRepository: TestAssessmentRepository

  @Autowired
  protected lateinit var testOffenderRepository: TestOffenderRepository

  @Autowired
  protected lateinit var jwtAuthHelper: JwtAuthorisationHelper

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  protected lateinit var offenderRepository: OffenderRepository

  @Autowired
  protected lateinit var assessmentRepository: AssessmentRepository

  @Autowired
  protected lateinit var assessmentEventRepository: AssessmentEventRepository

  @Autowired
  protected lateinit var addressDeletionEventRepository: AddressDeletionEventRepository

  @Autowired
  private lateinit var cacheManager: CacheManager

  @MockitoSpyBean
  protected lateinit var telemetryClient: TelemetryClient

  @MockitoSpyBean
  protected lateinit var transferPrisonService: TransferPrisonService

  @MockitoSpyBean
  lateinit var gotenbergApiClient: GotenbergApiClient

  protected fun <T> capture(argumentCaptor: ArgumentCaptor<T>): T = argumentCaptor.capture()

  internal fun setAuthorisation(
    username: String? = "AUTH_ADM",
    roles: List<String> = listOf(),
    scopes: List<String> = listOf("read"),
    agent: AgentDto? = null,
  ): (HttpHeaders) -> Unit = { headers ->
    jwtAuthHelper.setAuthorisationHeader(username = username, scope = scopes, roles = roles)(headers)
    headers.set("username", agent?.username)
    headers.set("role", agent?.role?.name)
    headers.set("fullName", agent?.fullName)
    headers.set("onBehalfOf", agent?.onBehalfOf)
  }

  protected fun stubPingWithResponse(status: Int) {
    hmppsAuth.stubHealthPing(status)
  }

  @BeforeEach
  fun evictAllCaches() {
    cacheManager.cacheNames.forEach { cacheManager.getCache(it)?.clear() }
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
