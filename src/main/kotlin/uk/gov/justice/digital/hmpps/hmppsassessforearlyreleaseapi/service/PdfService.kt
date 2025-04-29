package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.enum.DocumentSubjectType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.gotenberg.GotenbergApiClient
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.workingdays.WorkingDaysService
import java.time.LocalDate

@Service
class PdfService(
  private val thymeleafEngine: TemplateEngine,
  private val gotenbergApiClient: GotenbergApiClient,
  @Value("\${assessments.url}") private val assessmentsUrl: String,
  private val workingDaysService: WorkingDaysService,
  private val eligibilityAndSuitabilityService: EligibilityAndSuitabilityService,
) {

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
    const val WORKINGS_DAY_BEFORE_ADDRESS_FORM_DUE = 5
  }

  fun generateOffenderPdf(prisonNumber: String, documentSubjectType: DocumentSubjectType): ByteArray? {
    val templatePathAndFile = getTemplateFile(documentSubjectType)
    val data = HashMap<String, Any?>()

    data["assessmentsUrl"] = assessmentsUrl
    data["docSubjectType"] = documentSubjectType.name
    data["dateToday"] = LocalDate.now()

    if (documentSubjectType.includesSignAndName) {
      data["showSigned"] = true
      data["showName"] = true
    }
    if (documentSubjectType.includesGradeAndDate) {
      data["showGrade"] = true
      data["showDate"] = true
    }

    addAssessmentDetails(documentSubjectType, prisonNumber, data)

    val htmlContent = createHtmlContent(templatePathAndFile, data)
    return gotenbergApiClient.sendCreatePdfRequest(htmlContent)
  }

  private fun getTemplateFile(documentSubjectType: DocumentSubjectType): String {
    val pathNameParts = documentSubjectType.name.lowercase().split('_', limit = 2)
    return pathNameParts[0] + '/' + pathNameParts[1]
  }

  private fun addAssessmentDetails(
    documentSubjectType: DocumentSubjectType,
    prisonNumber: String,
    data: HashMap<String, Any?>,
  ) {
    val caseView = eligibilityAndSuitabilityService.getCaseView(prisonNumber)
    val currentAssessment = caseView.assessmentSummary
    data["currentAssessment"] = currentAssessment
    data["fullName"] = "${currentAssessment.forename} ${currentAssessment.surname}".convertToTitleCase()

    when (documentSubjectType) {
      DocumentSubjectType.OFFENDER_ELIGIBLE_FORM -> {
        data["taggingEndDate"] =
          currentAssessment.crd?.let { workingDaysService.workingDaysBefore(currentAssessment.crd).take(1).first() }
      }
      DocumentSubjectType.OFFENDER_ADDRESS_CHECKS_INFORMATION_FORM -> {
        data["addressFormDueDate"] = workingDaysService.workingDaysAfter(LocalDate.now()).take(WORKINGS_DAY_BEFORE_ADDRESS_FORM_DUE).last()
      }
      DocumentSubjectType.OFFENDER_ADDRESS_CHECKS_FORM,
      DocumentSubjectType.OFFENDER_OPT_OUT_FORM,
      DocumentSubjectType.OFFENDER_NOT_ELIGIBLE_FORM,
      -> {
        data["failedQuestionDescription"] = caseView.failedQuestionDescription.firstOrNull()
      }
      DocumentSubjectType.OFFENDER_ADDRESS_UNSUITABLE_FORM,
      DocumentSubjectType.OFFENDER_POSTPONED_FORM,
      -> {
        val reason: PostponeCaseReasonType? = currentAssessment.postponementReasons.firstOrNull()
        if (reason != null) {
          data["postponementReasonDescription"] = PostponeCaseReasonType.getDescription(reason)
        }
      }
      DocumentSubjectType.OFFENDER_NOT_ENOUGH_TIME_FORM,
      DocumentSubjectType.OFFENDER_APPROVED_FORM,
      DocumentSubjectType.OFFENDER_AGENCY_NOTIFICATION_FORM,
      DocumentSubjectType.OFFENDER_CANCEL_AGENCY_NOTIFICATION_FORM,
      DocumentSubjectType.OFFENDER_REFUSED_FORM,
      DocumentSubjectType.OFFENDER_NOT_SUITABLE_FORM,
      -> {
        data["failedQuestionDescription"] = caseView.failedQuestionDescription.firstOrNull()
      }
    }
  }

  private fun createHtmlContent(templatePathAndFile: String, details: Map<String, Any?>): String {
    log.debug("Creating html content using $templatePathAndFile")
    val context = Context()
    context.setVariables(details)
    return thymeleafEngine.process(templatePathAndFile, context)
  }
}
