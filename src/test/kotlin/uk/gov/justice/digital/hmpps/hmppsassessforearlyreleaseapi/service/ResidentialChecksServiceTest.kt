package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.validation.SimpleErrors
import org.springframework.validation.Validator
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks.ResidentialChecksTaskAnswer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentialChecksTaskAnswerRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aRiskManagementDecisionTaskAnswers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aStandardAddressCheckRequestWithAllChecksComplete
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aStandardAddressCheckRequestWithFewChecksFailed
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.inProgressStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.notStartedStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

class ResidentialChecksServiceTest {
  private val addressService = mock<AddressService>()
  private val assessmentService = mock<AssessmentService>()
  private val residentialChecksTaskAnswerRepository = mock<ResidentialChecksTaskAnswerRepository>()
  private val curfewAddressCheckRequestRepository = mock<CurfewAddressCheckRequestRepository>()
  private val objectMapper = jacksonObjectMapper().registerModule(
    JavaTimeModule(),
  )
  private val validator = mock<Validator>()

  private val residentialChecksService: ResidentialChecksService = ResidentialChecksService(
    addressService,
    assessmentService,
    residentialChecksTaskAnswerRepository,
    curfewAddressCheckRequestRepository,
    objectMapper,
    validator,
  )

  @Test
  fun `should get the status of the residential checks for an assessment`() {
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(anAssessmentSummary())

    whenever(residentialChecksTaskAnswerRepository.findByAddressCheckRequestId(ADDRESS_REQUEST_ID)).thenReturn(
      listOf(
        aRiskManagementDecisionTaskAnswers(
          criterionMet = true,
        ),
      ),
    )

    val residentialChecksView = residentialChecksService.getResidentialChecksView(PRISON_NUMBER, ADDRESS_REQUEST_ID)

    assertThat(residentialChecksView.assessmentSummary).isEqualTo(anAssessmentSummary())
    assertThat(residentialChecksView.overallStatus).isEqualTo(ResidentialChecksStatus.NOT_STARTED)
    assertThat(residentialChecksView.tasks).hasSize(6)
    residentialChecksView.tasks.forEach { task ->
      val expectedStatus = if (task.config.code == "make-a-risk-management-decision") {
        TaskStatus.SUITABLE
      } else {
        TaskStatus.NOT_STARTED
      }
      assertThat(task.status).isEqualTo(expectedStatus)
    }
  }

  @Test
  fun `should get the status of a residential checks task for an assessment`() {
    val taskCode = "assess-this-persons-risk"
    val assessmentSummary = anAssessmentSummary()
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(assessmentSummary)
    whenever(residentialChecksTaskAnswerRepository.findByAddressCheckRequestIdAndTaskCode(ADDRESS_REQUEST_ID, taskCode)).thenReturn(
      aRiskManagementDecisionTaskAnswers(
        criterionMet = false,
      ),
    )
    val residentialChecksTaskView =
      residentialChecksService.getResidentialChecksTask(PRISON_NUMBER, ADDRESS_REQUEST_ID, taskCode)

    assertThat(residentialChecksTaskView.assessmentSummary).isEqualTo(assessmentSummary)
    assertThat(residentialChecksTaskView.taskConfig.code).isEqualTo(taskCode)
    assertThat(residentialChecksTaskView.taskStatus).isEqualTo(TaskStatus.UNSUITABLE)
  }

  @Test
  fun `should throw an exception for an invalid task code`() {
    val taskCode = "not a valid task code"
    val assessmentSummary = anAssessmentSummary()
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(assessmentSummary)

    assertThrows<NoResourceFoundException> {
      residentialChecksService.getResidentialChecksTask(
        PRISON_NUMBER,
        ADDRESS_REQUEST_ID,
        taskCode,
      )
    }
  }

  @Test
  fun `should save residential checks task answers`() {
    val assessmentEntity = anOffender().assessments.first()
    val saveTaskAnswersRequest = SaveResidentialChecksTaskAnswersRequest(
      taskCode = "make-a-risk-management-decision",
      answers = mapOf(
        "canOffenderBeManagedSafely" to false,
        "informationToSupportDecision" to "info",
        "riskManagementPlanningActionsNeeded" to false,
      ),
      agent = PROBATION_COM_AGENT,
    )

    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(assessmentEntity)
    whenever(addressService.getCurfewAddressCheckRequest(ADDRESS_REQUEST_ID, PRISON_NUMBER)).thenReturn(
      aStandardAddressCheckRequestWithAllChecksComplete(),
    )
    whenever(residentialChecksTaskAnswerRepository.save(any())).thenAnswer { it.arguments[0] }
    whenever(validator.validateObject(any())).thenReturn(SimpleErrors("RiskManagementDecisionAnswers"))

    val answersSummary = residentialChecksService.saveResidentialChecksTaskAnswers(
      PRISON_NUMBER,
      ADDRESS_REQUEST_ID,
      saveTaskAnswersRequest,
    )

    verify(addressService).getCurfewAddressCheckRequest(ADDRESS_REQUEST_ID, PRISON_NUMBER)
    assertThat(answersSummary.addressCheckRequestId).isEqualTo(ADDRESS_REQUEST_ID)
    assertThat(answersSummary.taskCode).isEqualTo(saveTaskAnswersRequest.taskCode)
    argumentCaptor<ResidentialChecksTaskAnswer> {
      verify(residentialChecksTaskAnswerRepository).save(capture())
      with(firstValue) {
        assertThat(taskCode).isEqualTo(saveTaskAnswersRequest.taskCode)
        assertThat(toAnswersMap()).isEqualTo(saveTaskAnswersRequest.answers)
        assertThat(criterionMet).isFalse()
        assertThat(taskVersion).isEqualTo("V1")
        assertThat(createdTimestamp).isNotNull()
        assertThat(lastUpdatedTimestamp).isNotNull()
      }
    }
  }

  @Nested
  inner class GetAddressesCheckStatus {
    @Test
    fun `test all suitable`() {
      val result = residentialChecksService.getAddressesCheckStatus(listOf(aStandardAddressCheckRequestWithAllChecksComplete(), aStandardAddressCheckRequestWithFewChecksFailed()))
      assertThat(result).isEqualTo(ResidentialChecksStatus.SUITABLE)
    }

    @Test
    fun `test any unsuitable`() {
      val result = residentialChecksService.getAddressesCheckStatus(listOf(aStandardAddressCheckRequestWithFewChecksFailed(), aStandardAddressCheckRequestWithFewChecksFailed()))
      assertThat(result).isEqualTo(ResidentialChecksStatus.UNSUITABLE)
    }

    @Test
    fun `test in progress`() {
      val result = residentialChecksService.getAddressesCheckStatus(listOf(inProgressStandardAddressCheckRequest(), notStartedStandardAddressCheckRequest()))
      assertThat(result).isEqualTo(ResidentialChecksStatus.IN_PROGRESS)
    }

    @Test
    fun `test not started`() {
      val result = residentialChecksService.getAddressesCheckStatus(listOf(notStartedStandardAddressCheckRequest()))
      assertThat(result).isEqualTo(ResidentialChecksStatus.NOT_STARTED)
    }

    @Test
    fun `test mixed statuses`() {
      val result = residentialChecksService.getAddressesCheckStatus(listOf(aStandardAddressCheckRequestWithFewChecksFailed(), inProgressStandardAddressCheckRequest(), notStartedStandardAddressCheckRequest()))
      assertThat(result).isEqualTo(ResidentialChecksStatus.IN_PROGRESS)
    }
  }
}
