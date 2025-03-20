package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentOverviewSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus.UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate

const val DAYS_TO_ADD = 5L

@Component
class AssessmentToAssessmentOverviewSummaryMapper {

  fun map(assessment: Assessment, prisonName: String, prisonerSearchResults: PrisonerSearchPrisoner, eligibilityStatus: EligibilityStatus, suitabilityStatus: SuitabilityStatus): AssessmentOverviewSummary {
    val offender = assessment.offender
    return AssessmentOverviewSummary(
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prisonName,
      status = assessment.status,
      responsibleCom = assessment.responsibleCom?.toSummary(),
      team = assessment.team,
      policyVersion = assessment.policyVersion,
      optOutReasonType = assessment.optOutReasonType,
      optOutReasonOther = assessment.optOutReasonOther,
      cellLocation = prisonerSearchResults.cellLocation,
      mainOffense = prisonerSearchResults.mostSeriousOffence,
      tasks = assessment.status.tasks().mapValues { (_, tasks) ->
        tasks.map { TaskProgress(it.task, it.status(assessment)) }
      },
      toDoEligibilityAndSuitabilityBy = getToDoByDate(offender),
      result = determineResult(eligibilityStatus, suitabilityStatus),
    )
  }

  private fun getToDoByDate(offender: Offender): LocalDate {
    val createdDate = offender.createdTimestamp.toLocalDate()
    return createdDate.plusDays(DAYS_TO_ADD)
  }

  private fun determineResult(eligibilityStatus: EligibilityStatus, suitabilityStatus: SuitabilityStatus): String? = when {
    eligibilityStatus == INELIGIBLE && suitabilityStatus == NOT_STARTED -> "Ineligible"
    eligibilityStatus == ELIGIBLE && suitabilityStatus == UNSUITABLE -> "Unsuitable"
    eligibilityStatus == INELIGIBLE && suitabilityStatus == UNSUITABLE -> "Ineligible and Unsuitable"
    eligibilityStatus == ELIGIBLE && suitabilityStatus == SUITABLE -> "Eligible and Suitable"
    else -> null
  }
}
