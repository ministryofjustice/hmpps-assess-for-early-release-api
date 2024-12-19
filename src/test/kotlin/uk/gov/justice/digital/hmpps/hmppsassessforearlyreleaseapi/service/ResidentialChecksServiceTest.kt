package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.reactive.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.CurfewAddressCheckRequestRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.ResidentialChecksTaskAnswerRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.ADDRESS_REQUEST_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus
import java.util.Optional

class ResidentialChecksServiceTest {
  private val assessmentService = mock<AssessmentService>()
  private val curfewAddressCheckRequestRepository = mock<CurfewAddressCheckRequestRepository>()
  private val residentialChecksTaskAnswerRepository = mock<ResidentialChecksTaskAnswerRepository>()
  private val objectMapper = jacksonObjectMapper().registerModule(
    JavaTimeModule(),
  )

  private val residentialChecksService: ResidentialChecksService = ResidentialChecksService(
    assessmentService,
    curfewAddressCheckRequestRepository,
    residentialChecksTaskAnswerRepository,
    objectMapper,
  )

  @Test
  fun `should get the status of the residential checks for an assessment`() {
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(anAssessmentSummary())

    val residentialChecksView = residentialChecksService.getResidentialChecksView(PRISON_NUMBER, ADDRESS_REQUEST_ID)

    assertThat(residentialChecksView.assessmentSummary).isEqualTo(anAssessmentSummary())
    assertThat(residentialChecksView.overallStatus).isEqualTo(ResidentialChecksStatus.NOT_STARTED)
    assertThat(residentialChecksView.tasks).hasSize(6)
    assertThat(residentialChecksView.tasks).allMatch { it.status == TaskStatus.NOT_STARTED }
  }

  @Test
  fun `should get the status of a residential checks task for an assessment`() {
    val taskCode = "assess-this-persons-risk"
    val assessmentSummary = anAssessmentSummary()
    whenever(assessmentService.getCurrentAssessmentSummary(PRISON_NUMBER)).thenReturn(assessmentSummary)

    val residentialChecksTaskView =
      residentialChecksService.getResidentialChecksTask(PRISON_NUMBER, ADDRESS_REQUEST_ID, taskCode)

    assertThat(residentialChecksTaskView.assessmentSummary).isEqualTo(assessmentSummary)
    assertThat(residentialChecksTaskView.taskConfig.code).isEqualTo(taskCode)
    assertThat(residentialChecksTaskView.taskStatus).isEqualTo(TaskStatus.NOT_STARTED)
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
    val saveTaskAnswersRequest = SaveResidentialChecksTaskAnswersRequest(
      taskCode = "make-a-risk-management-decision",
      answers = mapOf(
        "canOffenderBeManagedSafely" to true,
        "informationToSupportDecision" to "info",
        "riskManagementPlanningActionsNeeded" to false,
      ),
    )

    whenever(curfewAddressCheckRequestRepository.findById(ADDRESS_REQUEST_ID)).thenReturn(
      Optional.of(
        aStandardAddressCheckRequest(),
      ),
    )
    whenever(residentialChecksTaskAnswerRepository.save(any())).thenAnswer { it.arguments[0] }

    val answersSummary = residentialChecksService.saveResidentialChecksTaskAnswers(
      PRISON_NUMBER,
      ADDRESS_REQUEST_ID,
      saveTaskAnswersRequest,
    )

    verify(curfewAddressCheckRequestRepository).findById(ADDRESS_REQUEST_ID)
    assertThat(answersSummary.addressCheckRequestId).isEqualTo(ADDRESS_REQUEST_ID)
    assertThat(answersSummary.taskCode).isEqualTo(saveTaskAnswersRequest.taskCode)
  }
}
