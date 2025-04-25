package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import org.springframework.validation.Validator
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.curfewAddress.CurfewAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.AnswerPayload
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswerType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.status
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.TaskAnswersValidationException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskAnswersSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentialChecksTaskAnswerRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.RESIDENTIAL_CHECKS_POLICY_V1
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.PolicyVersion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.Task
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskQuestion
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus.UNSUITABLE
import java.time.LocalDateTime

@Service
class ResidentialChecksService(
  private val addressService: AddressService,
  private val assessmentService: AssessmentService,
  private val residentialChecksTaskAnswerRepository: ResidentialChecksTaskAnswerRepository,
  private val curfewAddressCheckRequestRepository: CurfewAddressCheckRequestRepository,
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
        status = taskAnswers.status(),
        config = task,
        answers = answersMap,
      )
    }

    return ResidentialChecksView(currentAssessment, ResidentialChecksStatus.NOT_STARTED, tasks)
  }

  fun getResidentialChecksTask(prisonNumber: String, requestId: Long, taskCode: String): ResidentialChecksTaskView {
    val currentAssessment = assessmentService.getCurrentAssessmentSummary(prisonNumber)
    val taskConfig = getTaskConfig(taskCode)
    val taskAnswers = residentialChecksTaskAnswerRepository.findByAddressCheckRequestIdAndTaskCode(requestId, taskCode)
    val answersMap = taskAnswers?.toAnswersMap() ?: emptyMap()

    return ResidentialChecksTaskView(
      assessmentSummary = currentAssessment,
      taskConfig = taskConfig,
      taskStatus = taskAnswers.status(),
      answers = answersMap,
    )
  }

  @Transactional
  fun saveResidentialChecksTaskAnswers(
    prisonNumber: String,
    addressCheckRequestId: Long,
    saveTaskAnswersRequest: SaveResidentialChecksTaskAnswersRequest,
  ): ResidentialChecksTaskAnswersSummary {
    val taskVersion = PolicyVersion.V1.name
    val taskCode = saveTaskAnswersRequest.taskCode
    val answersMap = saveTaskAnswersRequest.answers

    val answers = getTaskAnswers(taskCode, answersMap)
    validateTaskAnswers(taskCode, answers)

    val criterionMet = areTaskCriterionMet(taskCode, answersMap)
    val existingAnswers = residentialChecksTaskAnswerRepository.findByAddressCheckRequestIdAndTaskCode(
      addressCheckRequestId,
      taskCode,
    )
    val addressCheckRequest = addressService.getCurfewAddressCheckRequest(addressCheckRequestId, prisonNumber)
    if (existingAnswers != null) {
      val updatedAnswers = existingAnswers.updateAnswers(answers)
      updatedAnswers.lastUpdatedTimestamp = LocalDateTime.now()
      updatedAnswers.criterionMet = criterionMet
      residentialChecksTaskAnswerRepository.save(updatedAnswers)
    } else {
      residentialChecksTaskAnswerRepository.save(answers.createTaskAnswersEntity(addressCheckRequest, criterionMet, taskVersion))
    }

    val assessment = assessmentService.getCurrentAssessment(prisonNumber)
    val checkRequests = curfewAddressCheckRequestRepository.findByAssessment(assessment)
    val checksStatus = getAddressesCheckStatus(checkRequests)
    assessmentService.updateAddressChecksStatus(prisonNumber, checksStatus, saveTaskAnswersRequest)

    return ResidentialChecksTaskAnswersSummary(
      addressCheckRequestId = addressCheckRequestId,
      taskCode = taskCode,
      answers = answersMap,
      taskVersion = taskVersion,
    )
  }

  private fun areTaskCriterionMet(taskCode: String, answers: Map<String, Any>): Boolean {
    val taskQuestions = getTaskQuestions(getTaskConfig(taskCode))
    var criterionMet = true
    for (question in taskQuestions) {
      val answer = answers[question.input.name]
      if (!question.criterionMet.evaluate(answer)) {
        criterionMet = false
        break
      }
    }
    return criterionMet
  }

  private fun getTaskConfig(taskCode: String): Task = RESIDENTIAL_CHECKS_POLICY_V1.tasks.find { it.code == taskCode }
    ?: throw NoResourceFoundException("$taskCode is not a valid task code")

  private fun getTaskQuestions(taskConfig: Task): List<TaskQuestion> = taskConfig.sections.flatMap {
    it.questions
  }

  private fun getPolicyTaskCodes(): List<String> = RESIDENTIAL_CHECKS_POLICY_V1.tasks.map { it.code }

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

  private fun isAddressSuitable(taskAnswers: MutableSet<ResidentialChecksTaskAnswer>): Boolean {
     val taskStatus = getPolicyTaskCodes().map { taskAnswers.find { answer -> answer.taskCode == it }?.toTaskStatus() }
     return taskStatus.all { it == SUITABLE }
  }

  fun getAddressesCheckStatus(addressCheckRequests: List<CurfewAddressCheckRequest>): ResidentialChecksStatus {
     return when {
       addressCheckRequests.any { isAddressSuitable(it.taskAnswers) } -> ResidentialChecksStatus.SUITABLE
       addressCheckRequests.any { it.taskAnswers.any { answer -> answer.toTaskStatus() == UNSUITABLE } } -> ResidentialChecksStatus.UNSUITABLE
       addressCheckRequests.any { it.taskAnswers.any { answer -> answer.toTaskStatus() == SUITABLE } } -> ResidentialChecksStatus.IN_PROGRESS
       else -> ResidentialChecksStatus.NOT_STARTED
       }
  }

  private fun ResidentialChecksTaskAnswer?.toTaskStatus(): TaskStatus = when (this?.criterionMet) {
    null -> NOT_STARTED
    true -> SUITABLE
    false -> UNSUITABLE
  }
}
