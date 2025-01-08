package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import java.nio.charset.StandardCharsets

@Service
class PdfService(
  private val templateEngine: TemplateEngine,
  private val gotenbergApiClient: GotenbergApiClient,
  @Value("\${assessments.url}") private val assessmentsUrl: String,
) {
  fun generateHtml(title: String, message: String): String {
    val context = Context()
    context.setVariable("title", title)
    context.setVariable("message", message)
    context.setVariable("assessmentsUrl", assessmentsUrl)
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

    return gotenbergApiClient.convertHtmlToPdf(requestEntity)
  }
}
