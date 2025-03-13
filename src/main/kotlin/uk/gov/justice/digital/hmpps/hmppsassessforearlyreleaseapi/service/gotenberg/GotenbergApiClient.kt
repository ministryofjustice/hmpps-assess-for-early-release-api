package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.gotenberg

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.PdfConfigProperties
import java.nio.charset.StandardCharsets

@Component
class GotenbergApiClient(
  @Qualifier("gotenbergClient") val gotenbergClient: WebClient,
  private val pdfConfigProperties: PdfConfigProperties,
) {

  private val uri = "/forms/chromium/convert/html"

  companion object {
    val LOG: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun sendCreatePdfRequest(htmlContent: String): ByteArray? {
    val httpEntity = HttpEntity(
      createPdfRequestData(htmlContent),
      createHttpsHeaders(),
    )

    return gotenbergClient
      .post()
      .uri(uri)
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .bodyValue(httpEntity.body!!)
      .retrieve()
      .bodyToMono(ByteArray::class.java)
      .onErrorResume {
        LOG.error("sendCreatePdfRequest Failed post Request: $htmlContent  URI: $uri Error: ${it.message} ", it)
        Mono.error(it)
      }.block()
  }

  private fun createHttpsHeaders(): HttpHeaders {
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA
    return headers
  }

  private fun createPdfRequestData(
    htmlContent: String,
  ): LinkedMultiValueMap<String, Any> {
    val documentData = HttpEntity(
      htmlContent.toByteArray(StandardCharsets.UTF_8),
      HttpHeaders().apply {
        contentType = MediaType.TEXT_HTML
        setContentDispositionFormData("files", "index.html")
      },
    )

    val body = LinkedMultiValueMap<String, Any>()
    body.add("files", documentData)
    body.add("paperWidth", pdfConfigProperties.paperWidth)
    body.add("paperHeight", pdfConfigProperties.paperHeight)
    body.add("marginTop", pdfConfigProperties.marginTop)
    body.add("marginBottom", pdfConfigProperties.marginBottom)
    body.add("marginLeft", pdfConfigProperties.marginLeft)
    body.add("marginRight", pdfConfigProperties.marginRight)
    return body
  }
}
