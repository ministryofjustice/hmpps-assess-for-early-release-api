package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

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

  fun sendCreatePdfRequest(htmlContent: String, documentFileName: String): ByteArray? {
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA

    val body = createPdfRequestData(htmlContent, documentFileName)

    return gotenbergClient
      .post()
      .uri("/forms/chromium/convert/html")
      .contentType(MediaType.MULTIPART_FORM_DATA)
      .bodyValue(HttpEntity(body, headers).body ?: LinkedMultiValueMap<String, Any>())
      .retrieve()
      .bodyToMono(ByteArray::class.java)
      .onErrorResume { Mono.error(it) }
      .block()
  }

  private fun createPdfRequestData(
    htmlContent: String,
    documentFileName: String,
  ): LinkedMultiValueMap<String, Any> {
    val documentData = HttpEntity(
      htmlContent.toByteArray(StandardCharsets.UTF_8),
      HttpHeaders().apply {
        contentType = MediaType.TEXT_HTML
        setContentDispositionFormData("files", documentFileName + ".pdf")
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
