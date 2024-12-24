package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration
//
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.AfterAll
//import org.junit.jupiter.api.BeforeAll
//import org.junit.jupiter.api.Nested
//import org.junit.jupiter.api.Test
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.test.context.DynamicPropertyRegistry
//import org.springframework.test.context.DynamicPropertySource
//import org.thymeleaf.TemplateEngine
//import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
//import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock.DeliusMockServer
//

//
//class PdfResourceIntTest : SqsIntegrationTestBase() {
//
//  @Nested
//  inner class GetStaffDetailsByUsername {
//    @Test
//    fun `should return unauthorized if no token`() {
//      webTestClient.get()
//        .uri(GET_PDF_URL)
//        .exchange()
//        .expectStatus()
//        .isUnauthorized
//    }
//
//    @Test
//    fun `should return forbidden if no role`() {
//      webTestClient.get()
//        .uri(GET_PDF_URL)
//        .headers(setAuthorisation())
//        .exchange()
//        .expectStatus()
//        .isForbidden
//    }
//
//    @Test
//    fun `should return forbidden if wrong role`() {
//      webTestClient.get()
//        .uri(GET_PDF_URL)
//        .headers(setAuthorisation(roles = listOf("ROLE_WRONG")))
//        .exchange()
//        .expectStatus()
//        .isForbidden
//    }
//
//    @Test
//    fun `should return pdf`() {
//      val pdfBytes = "PDF_BYTES".toByteArray()
//      deliusMockServer.enqueue(MockResponse().setBody("PDF_BYTES").addHeader("Content-Type", "application/pdf"))
//
//      val result = webTestClient.get()
//        .uri(GET_PDF_URL)
//        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
//        .exchange()
//        .expectStatus()
//        .isOk
//        .expectBody(ByteArray::class.java)
//        .returnResult().responseBody!!
//
//      assertThat(result).isEqualTo(pdfBytes)
//    }
//  }
//
//  @Configuration
//  class TestConfig {
//
//    @Bean
//    fun templateEngine(): TemplateEngine {
//      val templateResolver = ClassLoaderTemplateResolver()
//      templateResolver.prefix = "templates/"
//      templateResolver.suffix = ".html"
//      templateResolver.setTemplateMode("HTML")
//      templateResolver.characterEncoding = "UTF-8"
//      val templateEngine = TemplateEngine()
//      templateEngine.setTemplateResolver(templateResolver)
//      return templateEngine
//    }
//  }
//
//  private companion object {
////    val mockWebServer = MockWebServer()
////
////    @JvmStatic
////    @BeforeAll
////    fun startMocks() {
////      mockWebServer.start()
////    }
////
////    @JvmStatic
////    @AfterAll
////    fun stopMocks() {
////      mockWebServer.shutdown()
////    }
//
//    val deliusMockServer = DeliusMockServer(3002)
//
//    @JvmStatic
//    @BeforeAll
//    fun startMocks() {
//      deliusMockServer.start()
//    }
//
//    @JvmStatic
//    @AfterAll
//    fun stopMocks() {
//      deliusMockServer.stop()
//    }
//
//    @DynamicPropertySource
//    @JvmStatic
//    fun registerProperties(registry: DynamicPropertyRegistry) {
//      registry.add("http://localhost:3002/forms/chromium/convert/html") { deliusMockServer.url("/").toString() }
//    }
//  }
//}

import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.base.SqsIntegrationTestBase
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.PdfService

private const val TITLE = "title"
private const val MESSAGE = "MESSAGE"
private const val GET_PDF_URL = "/pdf?title=$TITLE&message=$MESSAGE"
@SpringBootTest
class PdfServiceIntegrationTest : SqsIntegrationTestBase() {

  @Autowired
  private lateinit var pdfService: PdfService


  @Test
  fun `generatePdf should return PDF bytes`() {
    val pdfBytes = "PDF_BYTES".toByteArray()
    mockWebServer.enqueue(MockResponse().setBody("PDF_BYTES").addHeader("Content-Type", "application/pdf"))

//    val result = pdfService.generatePdf("Test Title", "Test Message")

          val result = webTestClient.get()
        .uri(GET_PDF_URL)
        .headers(setAuthorisation(roles = listOf("ASSESS_FOR_EARLY_RELEASE_ADMIN")))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody(ByteArray::class.java)
        .returnResult().responseBody!!

//      assertThat(result).isEqualTo(pdfBytes)

    assertThat(result).isEqualTo(pdfBytes)
  }
  companion object {
    private lateinit var mockWebServer: MockWebServer

    @BeforeAll
    @JvmStatic
    fun setUp() {
      mockWebServer = MockWebServer()
      mockWebServer.start(3002)
    }

    @AfterAll
    @JvmStatic
    fun tearDown() {
      mockWebServer.shutdown()
    }

    @DynamicPropertySource
    @JvmStatic
    fun registerProperties(registry: DynamicPropertyRegistry) {
      registry.add("http://localhost:3002/forms/chromium/convert/html") { mockWebServer.url("http://localhost:3002/forms/chromium/convert/html").toString() }
    }
  }

}

//@Configuration
//class TestConfig {
//
//  @Bean
//  fun templateEngine(): TemplateEngine {
//    val templateResolver = ClassLoaderTemplateResolver()
//    templateResolver.prefix = "templates/"
//    templateResolver.suffix = ".html"
//    templateResolver.setTemplateMode("HTML")
//    templateResolver.characterEncoding = "UTF-8"
//    val templateEngine = TemplateEngine()
//    templateEngine.setTemplateResolver(templateResolver)
//    return templateEngine
//  }
//}