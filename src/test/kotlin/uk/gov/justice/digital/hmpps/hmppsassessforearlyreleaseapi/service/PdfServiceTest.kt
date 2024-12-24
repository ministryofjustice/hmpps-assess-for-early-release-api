package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.web.client.RestTemplate
import org.thymeleaf.TemplateEngine

class PdfServiceTest {
  private val templateEngine: TemplateEngine = TemplateEngine()
  private val service = PdfService(templateEngine, RestTemplate(), "gotenberg.api.url")
  @Nested
  inner class `generate pdf tests` {
    @Test
    fun `generate pdf`() {
      assertThat(service.generatePdf("title", "message")).isEqualTo("Hello, world!".toByteArray())
    }
  }
}
