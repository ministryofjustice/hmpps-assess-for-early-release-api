package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.validation.Validator
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.AnswerPayload
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
import java.time.LocalDateTime

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

    val taskAnswersForAddressCheck =
      residentialChecksTaskAnswerRepository.findByAddressCheckRequestId(addressCheckRequestId)

    val tasks = RESIDENTIAL_CHECKS_POLICY_V1.tasks.map { task ->
      val taskAnswers = taskAnswersForAddressCheck.find { it.taskCode == task.code }
      val answersMap = taskAnswers?.toAnswersMap() ?: emptyMap()
      ResidentialChecksTaskProgress(
        code = task.code,
        taskName = task.name,
        status = TaskStatus.NOT_STARTED,
        answers = answersMap,
      )
    }

    return ResidentialChecksView(currentAssessment, ResidentialChecksStatus.NOT_STARTED, tasks)
  }

  fun getResidentialChecksTask(prisonNumber: String, requestId: Long, taskCode: String): ResidentialChecksTaskView {
    val currentAssessment = assessmentService.getCurrentAssessmentSummary(prisonNumber)
    val task = RESIDENTIAL_CHECKS_POLICY_V1.tasks.find { it.code == taskCode }
      ?: throw NoResourceFoundException("$taskCode is not a valid task code")

    val taskAnswers = residentialChecksTaskAnswerRepository.findByAddressCheckRequestIdAndTaskCode(requestId, taskCode)
    val answersMap = taskAnswers?.toAnswersMap() ?: emptyMap()

    return ResidentialChecksTaskView(
      assessmentSummary = currentAssessment,
      taskConfig = task,
      taskStatus = TaskStatus.NOT_STARTED,
      answers = answersMap,
    )
  }

  fun saveResidentialChecksTaskAnswers(
    prisonNumber: String,
    addressCheckRequestId: Long,
    saveTaskAnswersRequest: SaveResidentialChecksTaskAnswersRequest,
  ): ResidentialChecksTaskAnswersSummary {
    val taskVersion = PolicyVersion.V1.name
    val addressCheckRequest = addressService.getCurfewAddressCheckRequest(addressCheckRequestId, prisonNumber)

    val answers = getTaskAnswers(saveTaskAnswersRequest.taskCode, saveTaskAnswersRequest.answers)
    validateTaskAnswers(saveTaskAnswersRequest.taskCode, answers)

    val existingAnswers = residentialChecksTaskAnswerRepository.findByAddressCheckRequestIdAndTaskCode(
      addressCheckRequestId,
      saveTaskAnswersRequest.taskCode,
    )

    if (existingAnswers != null) {
      val updatedAnswers = existingAnswers.updateAnswers(answers)
      updatedAnswers.lastUpdatedTimestamp = LocalDateTime.now()
      residentialChecksTaskAnswerRepository.save(updatedAnswers)
    } else {
      residentialChecksTaskAnswerRepository.save(answers.createTaskAnswersEntity(addressCheckRequest, taskVersion))
    }

    return ResidentialChecksTaskAnswersSummary(
      addressCheckRequestId = addressCheckRequestId,
      taskCode = saveTaskAnswersRequest.taskCode,
      answers = saveTaskAnswersRequest.answers,
      taskVersion = taskVersion,
    )
  }

  private fun getTaskAnswers(
    taskCode: String,
    answers: Map<String, Any>,
  ): AnswerPayload {
    val taskCodeClass = ResidentialChecksTaskAnswerType.getByTaskCode(taskCode).taskAnswerClass
    return objectMapper.convertValue(
      answers,
      taskCodeClass,
    )
  }

  private fun validateTaskAnswers(taskCode: String, answers: AnswerPayload) {
    val validationErrors = validator.validateObject(answers)
    if (validationErrors.hasErrors()) {
      throw TaskAnswersValidationException(taskCode, validationErrors)
    }
  }
}
