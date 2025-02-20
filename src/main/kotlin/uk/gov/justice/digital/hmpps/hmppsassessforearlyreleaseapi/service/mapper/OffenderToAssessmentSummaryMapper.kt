package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper

import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.PrisonerNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.TaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService

@Component
class OffenderToAssessmentSummaryMapper(
  private val prisonService: PrisonService,
) {

  fun map(offender: Offender): AssessmentSummary {
    val currentAssessment = offender.currentAssessment()
    val prisonerSearchResults = prisonService.searchPrisonersByNomisIds(listOf(offender.prisonNumber))
    if (prisonerSearchResults.isEmpty()) {
      throw PrisonerNotFoundException(offender.prisonNumber)
    }
    val prisonerDetails = prisonerSearchResults.first()
    return AssessmentSummary(
      forename = offender.forename,
      surname = offender.surname,
      dateOfBirth = offender.dateOfBirth,
      prisonNumber = offender.prisonNumber,
      hdced = offender.hdced,
      crd = offender.crd,
      location = prisonerDetails.prisonName,
      status = currentAssessment.status,
      responsibleCom = currentAssessment.responsibleCom?.toSummary(),
      team = currentAssessment.team,
      policyVersion = currentAssessment.policyVersion,
      optOutReasonType = currentAssessment.optOutReasonType,
      optOutReasonOther = currentAssessment.optOutReasonOther,
      cellLocation = prisonerDetails.cellLocation,
      mainOffense = prisonerDetails.mostSeriousOffence,
      tasks = currentAssessment.status.tasks().mapValues { (_, tasks) ->
        tasks.map { TaskProgress(it.task, it.status(currentAssessment)) }
      },
    )
  }
}
