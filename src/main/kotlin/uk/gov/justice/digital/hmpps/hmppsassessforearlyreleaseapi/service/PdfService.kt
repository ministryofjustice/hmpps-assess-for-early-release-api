package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.PdfConfigProperties
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum.DocumentSubjectType
import java.nio.charset.StandardCharsets

@Service
class PdfService(
  private val thymeleafEngine: TemplateEngine,
  private val gotenbergApiClient: GotenbergApiClient,
  @Value("\${assessments.url}") private val assessmentsUrl: String,
  private val pdfConfigProperties: PdfConfigProperties,
  private val assessmentService: AssessmentService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun generateOffenderPdf(prisonNumber: String, documentSubjectType: DocumentSubjectType): ByteArray? {
    val templatePathAndFile = getTemplateFile(documentSubjectType)
    val data = HashMap<String, Any>()

    data["currentAssessment"] = documentSubjectType.name
    data["assessmentsUrl"] = assessmentsUrl
    data["docSubjectType"] = documentSubjectType.name

    addAssessmentDetails(documentSubjectType, prisonNumber, data)

    val htmlContent = createHtmlContent(templatePathAndFile, data)
    return createPdf(htmlContent, documentSubjectType)
  }

  private fun getTemplateFile(documentSubjectType: DocumentSubjectType): String {
    val directoryNameEnd = documentSubjectType.name.lowercase().split('_', limit = 2)
    return directoryNameEnd[0] + '/' + directoryNameEnd[1]
  }

  private fun addAssessmentDetails(
    documentSubjectType: DocumentSubjectType,
    prisonNumber: String,
    data: HashMap<String, Any>,
  ) {
    when (documentSubjectType) {
      DocumentSubjectType.OFFENDER_ELIGIBLE_FORM,
      DocumentSubjectType.OFFENDER_ADDRESS_CHECKS_INFORMATION_FORM,
      DocumentSubjectType.OFFENDER_ADDRESS_CHECKS_FORM,
      DocumentSubjectType.OFFENDER_OPT_OUT_FORM,
      DocumentSubjectType.OFFENDER_NOT_ELIGIBLE_FORM,
      DocumentSubjectType.OFFENDER_ADDRESS_UNSUITABLE_FORM,
      DocumentSubjectType.OFFENDER_POSTPONED_FORM,
      DocumentSubjectType.OFFENDER_NOT_ENOUGH_TIME_FORM,
      DocumentSubjectType.OFFENDER_APPROVED_FORM,
      DocumentSubjectType.OFFENDER_AGENCY_NOTIFICATION_FORM,
      DocumentSubjectType.OFFENDER_CANCEL_AGENCY_NOTIFICATION_FORM,
      DocumentSubjectType.OFFENDER_REFUSED_FORM,
      DocumentSubjectType.OFFENDER_NOT_SUITABLE_FORM,
      -> {
        val currentAssessment = assessmentService.getCurrentAssessmentSummary(prisonNumber)
        data["currentAssessment"] = currentAssessment
      }
    }
  }

  private fun createHtmlContent(templatePathAndFile: String, details: Map<String, Any>): String {
    log.debug("Creating html content using $templatePathAndFile")
    val context = Context()
    context.setVariables(details)
    return thymeleafEngine.process(templatePathAndFile, context)
  }

  private fun createPdf(htmlContent: String, documentSubjectType: DocumentSubjectType): ByteArray? {
    val headers = HttpHeaders()
    headers.contentType = MediaType.MULTIPART_FORM_DATA

    val documentData = HttpEntity(
      htmlContent.toByteArray(StandardCharsets.UTF_8),
      HttpHeaders().apply {
        contentType = MediaType.TEXT_HTML
        setContentDispositionFormData("files", documentSubjectType.name.lowercase() + ".pdf")
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

    return gotenbergApiClient.requestPdf(HttpEntity(body, headers))
  }
}
