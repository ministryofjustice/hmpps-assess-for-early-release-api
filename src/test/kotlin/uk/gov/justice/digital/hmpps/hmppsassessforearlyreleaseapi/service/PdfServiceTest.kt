package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.thymeleaf.TemplateEngine

class PdfServiceTest {

  private var templateEngine = mock<TemplateEngine>()
  private val gotenbergClient = mock<GotenbergApiClient>()
  private val assessmentsUrl = "http://host.docker.internal:8089"

  private val service: PdfService =
    PdfService(
      templateEngine,
      gotenbergClient,
      assessmentsUrl,
    )

  @Test
  fun `generatePdf should return PDF bytes`() {
    val title = "Test Title"
    val message = "Test Message"
    val htmlContent = "<html><head><link rel=\"stylesheet\" th:href=\"${assessmentsUrl} + '/css/style.css'\"></head><body><h1>$title</h1><p>$message</p></body></html>"

    whenever(templateEngine.process(eq("sample"), any())).thenReturn(htmlContent)
    whenever(gotenbergClient.convertHtmlToPdf(any())).thenReturn("PDF_BYTES".toByteArray())

    assertThat(service.generatePdf(title, message)).isEqualTo("PDF_BYTES".toByteArray())
  }
}
