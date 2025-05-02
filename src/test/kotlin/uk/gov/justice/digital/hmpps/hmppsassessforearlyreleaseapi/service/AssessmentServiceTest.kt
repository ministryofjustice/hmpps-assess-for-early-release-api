package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.AWAITING_PRE_DECISION_CHECKS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType.NO_REASON_GIVEN
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_CA_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PROBATION_COM_AGENT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.Progress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithCompleteEligibilityChecks
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithSomeProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.saveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.client.mangeUsers.ManagedUsersService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.AssessmentToAssessmentOverviewSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.mapper.OffenderToAssessmentSummaryMapper
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.ResidentialChecksStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDate

class AssessmentServiceTest {
  private val prisonService = mock<PrisonService>()
  private val assessmentRepository = mock<AssessmentRepository>()
  private val policyService = PolicyService()
  private val probationService = mock<ProbationService>()
  private val staffRepository = mock<StaffRepository>()
  private val managedUsersService = mock<ManagedUsersService>()

  private val offenderToAssessmentSummaryMapper = OffenderToAssessmentSummaryMapper(prisonService)
  private val assessmentToAssessmentOverviewSummaryMapper = AssessmentToAssessmentOverviewSummaryMapper()

  private val service =
    AssessmentService(
      assessmentRepository,
      offenderToAssessmentSummaryMapper,
      assessmentToAssessmentOverviewSummaryMapper,
      prisonService,
      policyService,
      staffRepository,
      managedUsersService,
      probationService,
    )

  @Test
  fun `should get an offenders current assessment`() {
    // Given
    val hdced = LocalDate.now().plusDays(5)
    val offender = anOffender(hdced)
    mockGetNonDeletedAssessments(offender)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

    // When
    val assessment = service.getCurrentAssessmentSummary(PRISON_NUMBER)

    // Then
    verify(assessmentRepository).findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(PRISON_NUMBER)
    assertThat(assessment).isNotNull
    assertThat(assessment).extracting(
      "forename",
      "surname",
      "prisonNumber",
      "hdced",
      "crd",
      "location",
      "status",
    ).isEqualTo(listOf(FORENAME, SURNAME, PRISON_NUMBER, hdced, null, PRISON_NAME, NOT_STARTED))
  }

  @Test
  fun `should get an offenders current assessment overview`() {
    // Given
    val anAssessmentWithEligibilityProgress = anAssessmentWithSomeProgress(
      ELIGIBLE_AND_SUITABLE,
      eligibilityProgress = Progress.allSuccessful(),
      suitabilityProgress = Progress.allSuccessful(),
    )
    val offender = anAssessmentWithEligibilityProgress.offender
    mockGetNonDeletedAssessments(offender)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(listOf(aPrisonerSearchPrisoner()))
    whenever(prisonService.getPrisonNameForId(anyString())).thenReturn(PRISON_NAME)

    // When
    val assessment = service.getAssessmentOverviewSummary(PRISON_NUMBER)

    // Then
    verify(assessmentRepository).findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(PRISON_NUMBER)
    assertThat(assessment).isNotNull
    assertThat(assessment).extracting(
      "forename",
      "surname",
      "prisonNumber",
      "hdced",
      "crd",
      "location",
      "status",
      "toDoEligibilityAndSuitabilityBy",
      "result",
    ).isEqualTo(
      listOf(
        FORENAME, SURNAME, PRISON_NUMBER,
        LocalDate.now().plusDays(7), null, PRISON_NAME, ELIGIBLE_AND_SUITABLE, LocalDate.now().plusDays(5), "Eligible and Suitable",
      ),
    )
  }

  @Test
  fun `should throw exception when prisoner details not found`() {
    // Given
    val prisonNumber = "A1234AA"
    val offender = anOffender()
    mockGetNonDeletedAssessments(offender)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(prisonNumber))).thenReturn(emptyList())

    // When / Then
    val exception = org.junit.jupiter.api.assertThrows<ItemNotFoundException> {
      service.getAssessmentOverviewSummary(prisonNumber)
    }
    assertThat(exception.message).isEqualTo("Could not find prisoner details for $prisonNumber")
  }

  @Test
  fun `should opt-out an offender`() {
    val offender = anAssessmentWithCompleteEligibilityChecks(status = AWAITING_ADDRESS_AND_RISK_CHECKS).offender

    mockGetNonDeletedAssessments(offender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.optOut(PRISON_NUMBER, OptOutRequest(reasonType = NO_REASON_GIVEN, agent = PRISON_CA_AGENT))

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(OPTED_OUT)
    assertThat(assessmentCaptor.value.previousStatus).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should opt-in an offender`() {
    val assessment =
      anAssessmentWithCompleteEligibilityChecks(status = OPTED_OUT, previousStatus = AWAITING_ADDRESS_AND_RISK_CHECKS)

    mockGetNonDeletedAssessments(assessment.offender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.optBackIn(PRISON_NUMBER, PRISON_CA_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should submit an assessment for address checks`() {
    val anOffender = anOffender()
    anOffender.assessments.first().status = ELIGIBLE_AND_SUITABLE

    mockGetNonDeletedAssessments(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.submitAssessmentForAddressChecks(PRISON_NUMBER, PRISON_CA_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_ADDRESS_AND_RISK_CHECKS)
  }

  @Test
  fun `should submit an assessment for pre-decision checks`() {
    val anOffender = anOffender()
    anOffender.assessments.first().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS

    mockGetNonDeletedAssessments(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.submitForPreDecisionChecks(PRISON_NUMBER, PROBATION_COM_AGENT)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AWAITING_PRE_DECISION_CHECKS)
  }

  @Test
  fun `should update address checks status when checks complete`() {
    val anOffender = anOffender()
    anOffender.assessments.first().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS

    mockGetNonDeletedAssessments(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.SUITABLE, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isTrue()
  }

  @Test
  fun `should not update address checks status when checks already complete`() {
    val anOffender = anOffender()
    anOffender.assessments.first().status = ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
    anOffender.assessments.first().addressChecksComplete = true

    mockGetNonDeletedAssessments(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.SUITABLE, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isTrue()
  }

  @Test
  fun `should update address checks status to in progress`() {
    val anOffender = anOffender()
    anOffender.assessments.first().status = AWAITING_ADDRESS_AND_RISK_CHECKS

    mockGetNonDeletedAssessments(anOffender)
    whenever(prisonService.getPrisonNameForId(PRISON_ID)).thenReturn(PRISON_NAME)

    service.updateAddressChecksStatus(PRISON_NUMBER, ResidentialChecksStatus.IN_PROGRESS, saveResidentialChecksTaskAnswersRequest)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    assertThat(assessmentCaptor.value.addressChecksComplete).isFalse()
  }

  private fun mockGetNonDeletedAssessments(offender: Offender) {
    whenever(
      assessmentRepository.findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(
        PRISON_NUMBER,
      ),
    ).thenReturn(offender.assessments.toList())
  }

  @Test
  fun `should update the team on assessments when their com is updated`() {
    val com = aCommunityOffenderManager()
    val newTeamCode = "N68ABC"
    val assessment = Assessment(
      offender = anOffender(),
      responsibleCom = com,
      bookingId = BOOKING_ID,
      hdced = LocalDate.now().plusDays(5),
    )
    whenever(assessmentRepository.findByResponsibleComStaffCodeAndStatusInAndDeletedTimestampIsNull(com.staffCode, AssessmentStatus.inFlightStatuses())).thenReturn(
      listOf(assessment),
    )

    service.updateTeamForResponsibleCom(com.staffCode, newTeamCode)

    val assessmentCaptor = argumentCaptor<List<Assessment>>()
    verify(assessmentRepository).findByResponsibleComStaffCodeAndStatusInAndDeletedTimestampIsNull(com.staffCode, AssessmentStatus.inFlightStatuses())
    verify(assessmentRepository).saveAll(assessmentCaptor.capture())
    assessmentCaptor.firstValue.map { assertThat(it.teamCode).isEqualTo(newTeamCode) }
  }

  @Test
  fun `should create an assessment`() {
    val hdced = LocalDate.now().plusDays(1)
    val crd = LocalDate.now().plusDays(1)
    val sentenceStartDate = LocalDate.now().plusDays(1)

    val prisonerNumber = "A1234AA"
    val bookingId = 123456L
    val offender = anOffender()
    val offenderManager = aDeliusOffenderManager()
    val com = aCommunityOffenderManager()

    val mockAssessment = mock(Assessment::class.java)
    whenever(probationService.getCurrentResponsibleOfficer(any())).thenReturn(offenderManager)
    whenever(staffRepository.findByStaffCode(any())).thenReturn(com)
    whenever(assessmentRepository.save(any())).thenReturn(mockAssessment)

    service.createAssessment(offender, prisonerNumber, bookingId, hdced, crd, sentenceStartDate)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())

    val assessment = assessmentCaptor.value
    assertThat(hdced).isEqualTo(assessment.hdced)
    assertThat(crd).isEqualTo(assessment.crd)
    assertThat(sentenceStartDate).isEqualTo(assessment.sentenceStartDate)
  }

  @Test
  fun `should update assessment dates`() {
    val hdced = LocalDate.now().plusDays(1)
    val crd = LocalDate.now().plusDays(1)
    val sentenceStartDate = LocalDate.now().plusDays(1)

    val prisonerNumber = "A1234AA"
    val bookingId = 123456L
    val offender = anOffender()
    val offenderManager = aDeliusOffenderManager()
    val com = aCommunityOffenderManager()

    val mockAssessment = mock(Assessment::class.java)
    whenever(probationService.getCurrentResponsibleOfficer(any())).thenReturn(offenderManager)
    whenever(staffRepository.findByStaffCode(any())).thenReturn(com)
    whenever(assessmentRepository.save(any())).thenReturn(mockAssessment)

    service.createAssessment(offender, prisonerNumber, bookingId, hdced, crd, sentenceStartDate)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository, times(1)).save(assessmentCaptor.capture())

    val assessment = assessmentCaptor.value
    assertThat(hdced).isEqualTo(assessment.hdced)
    assertThat(crd).isEqualTo(assessment.crd)
    assertThat(sentenceStartDate).isEqualTo(assessment.sentenceStartDate)
  }
}
