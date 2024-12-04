package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.stereotype.Service
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentialChecksTaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentialChecksTaskView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.ResidentialChecksView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.RESIDENTIAL_CHECKS_POLICY_V1
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

@Service
class ResidentialChecksService(
  private val assessmentService: AssessmentService,
) {
  fun getResidentialChecksView(prisonNumber: String, addressCheckRequestId: Long): ResidentialChecksView {
    val currentAssessment = assessmentService.getCurrentAssessmentSummary(prisonNumber)

    val tasks = RESIDENTIAL_CHECKS_POLICY_V1.tasks.map { task ->
      ResidentialChecksTaskProgress(
        code = task.code,
        taskName = task.name,
        status = TaskStatus.NOT_STARTED,
      )
    }

    return ResidentialChecksView(currentAssessment, ResidentialChecksStatus.NOT_STARTED, tasks)
  }

  fun getResidentialChecksTask(prisonNumber: String, requestId: Long, taskCode: String): ResidentialChecksTaskView {
    val currentAssessment = assessmentService.getCurrentAssessmentSummary(prisonNumber)
    val task = RESIDENTIAL_CHECKS_POLICY_V1.tasks.find { it.code == taskCode }
      ?: throw NoResourceFoundException("$taskCode is not a valid task code")

    return ResidentialChecksTaskView(
      assessmentSummary = currentAssessment,
      taskConfig = task,
      taskStatus = TaskStatus.NOT_STARTED,
    )
  }
}
