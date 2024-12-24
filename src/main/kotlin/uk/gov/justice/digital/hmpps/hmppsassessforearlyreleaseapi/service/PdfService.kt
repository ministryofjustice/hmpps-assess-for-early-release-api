package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.nio.charset.StandardCharsets

@Service
class PdfService(
  private val templateEngine: TemplateEngine,
  private val restTemplate: RestTemplate,
  @Value("\${gotenberg.api.url}") private val gotenbergHost: String,
) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  fun generateHtml(title: String, message: String): String {
    val context = Context()
    context.setVariable("title", title)
    context.setVariable("message", message)
    return templateEngine.process("sample", context)
  }

  fun generatePdf(title: String, message: String): ByteArray? {
    val htmlContent = generateHtml(title, message)

    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA

    val body = LinkedMultiValueMap<String, Any>()
    body.add(
      "files",
      HttpEntity(
        htmlContent.toByteArray(StandardCharsets.UTF_8),
        HttpHeaders().apply {
          contentType = MediaType.TEXT_HTML
          setContentDispositionFormData("files", "index.html")
        },
      ),
    )
    body.add("paperWidth", "8.27")
    body.add("paperHeight", "11.69")
    body.add("marginTop", "1")
    body.add("marginBottom", "1")
    body.add("marginLeft", "1")
    body.add("marginRight", "1")

    val requestEntity = HttpEntity(body, headers)

    return try {
      val response: ResponseEntity<ByteArray> = restTemplate.postForEntity(
        "$gotenbergHost/forms/chromium/convert/html",
        requestEntity,
        ByteArray::class.java,
      )
      response.body
    } catch (e: HttpClientErrorException) {
      log.error("Error converting HTML to PDF", e)
      null
    }
  }
}
