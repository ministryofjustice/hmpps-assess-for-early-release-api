package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.HttpEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import org.thymeleaf.TemplateEngine

class PdfServiceTest {

  private var templateEngine = mock<TemplateEngine>()
  private var restTemplate = mock<RestTemplate>()
  private var gotenbergHost: String = "http://localhost:3002"

  private val service: PdfService =
    PdfService(
      templateEngine,
      restTemplate,
      gotenbergHost,
    )

  @Test
  fun `generatePdf should return PDF bytes`() {
    val title = "Test Title"
    val message = "Test Message"
    val htmlContent = "<html><body><h1>$title</h1><p>$message</p></body></html>"

    whenever(templateEngine.process(eq("sample"), any())).thenReturn(htmlContent)

    whenever(
      restTemplate.postForEntity(
        any<String>(),
        any<HttpEntity<*>>(),
        eq(ByteArray::class.java),
      ),
    ).thenReturn(ResponseEntity.ok("PDF_BYTES".toByteArray()))

    assertThat(service.generatePdf("title", "message")).isEqualTo("PDF_BYTES".toByteArray())
  }
}
