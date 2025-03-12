package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum.DocumentSubjectType

@Service
class PdfService(
  private val thymeleafEngine: TemplateEngine,
  private val gotenbergApiClient: GotenbergApiClient,
  @Value("\${assessments.url}") private val assessmentsUrl: String,
  private val assessmentService: AssessmentService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }

  fun generateOffenderPdf(prisonNumber: String, documentSubjectType: DocumentSubjectType): ByteArray? {
    val templatePathAndFile = getTemplateFile(documentSubjectType)
    val data = HashMap<String, Any>()

    data["assessmentsUrl"] = assessmentsUrl
    data["docSubjectType"] = documentSubjectType.name

    addAssessmentDetails(documentSubjectType, prisonNumber, data)

    val htmlContent = createHtmlContent(templatePathAndFile, data)
    return createPdf(htmlContent, documentSubjectType)
  }

  private fun getTemplateFile(documentSubjectType: DocumentSubjectType): String {
    val pathNameParts = documentSubjectType.name.lowercase().split('_', limit = 2)
    return pathNameParts[0] + '/' + pathNameParts[1]
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
    val documentFileName = documentSubjectType.name.lowercase()
    return gotenbergApiClient.sendCreatePdfRequest(htmlContent, documentFileName)
  }
}
