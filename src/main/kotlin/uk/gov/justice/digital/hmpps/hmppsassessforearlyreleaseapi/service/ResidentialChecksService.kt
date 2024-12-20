package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.validation.Validator
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswerType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.TaskAnswersValidationException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskAnswersSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentialChecksTaskAnswerRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.RESIDENTIAL_CHECKS_POLICY_V1
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.PolicyVersion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.AddressService

@Service
class ResidentialChecksService(
  private val addressService: AddressService,
  private val assessmentService: AssessmentService,
  private val residentialChecksTaskAnswerRepository: ResidentialChecksTaskAnswerRepository,
  private val objectMapper: ObjectMapper,
  private val validator: Validator,
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

  fun saveResidentialChecksTaskAnswers(
    prisonNumber: String,
    addressCheckRequestId: Long,
    saveTaskAnswersRequest: SaveResidentialChecksTaskAnswersRequest,
  ): ResidentialChecksTaskAnswersSummary {
    val taskVersion = PolicyVersion.V1.name
    val addressCheckRequest = addressService.getCurfewAddressCheckRequest(addressCheckRequestId, prisonNumber)

    val entity = transformToAnswersEntity(
      addressCheckRequest,
      saveTaskAnswersRequest.taskCode,
      taskVersion,
      saveTaskAnswersRequest.answers,
    )

    val answersEntity = residentialChecksTaskAnswerRepository.save(entity)
    return ResidentialChecksTaskAnswersSummary(
      answersId = answersEntity.id,
      addressCheckRequestId = addressCheckRequestId,
      taskCode = answersEntity.taskCode,
      answers = saveTaskAnswersRequest.answers,
      taskVersion = answersEntity.taskVersion,
    )
  }

  private fun transformToAnswersEntity(
    addressCheckRequest: CurfewAddressCheckRequest,
    taskCode: String,
    taskVersion: String,
    answers: Map<String, Any>,
  ): ResidentialChecksTaskAnswer {
    val taskCodeClass = ResidentialChecksTaskAnswerType.getByTaskCode(taskCode).taskAnswerClass

    val taskAnswers = objectMapper.convertValue(
      answers,
      taskCodeClass,
    )

    val validationErrors = validator.validateObject(taskAnswers)
    if (validationErrors.hasErrors()) {
      throw TaskAnswersValidationException(taskCode, validationErrors)
    }

    return taskAnswers.createTaskAnswersEntity(addressCheckRequest, taskVersion)
  }
}
