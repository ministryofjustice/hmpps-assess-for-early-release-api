package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration

import com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cache.CacheManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.PrisonRegisterMockServer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService

class PrisonRegisterServiceTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var cacheManager: CacheManager

  @Autowired
  lateinit var prisonRegisterService: PrisonRegisterService

  @BeforeEach
  @AfterEach
  fun clearCache() {
    cacheManager.getCache("prisons")?.clear()
  }

  @Test
  fun `should cache prison details`() {
    prisonRegisterMockServer.stubGetPrisons()

    prisonRegisterService.getPrisonIdsAndNames()
    prisonRegisterService.getPrisonIdsAndNames()

    prisonRegisterMockServer.verify(1, getRequestedFor(urlEqualTo("/prisons")))
  }

  private companion object {
    val prisonRegisterMockServer = PrisonRegisterMockServer()

    @JvmStatic
    @BeforeAll
    fun startMocks() {
      prisonRegisterMockServer.start()
    }

    @JvmStatic
    @AfterAll
    fun stopMocks() {
      prisonRegisterMockServer.stop()
    }
  }
}
