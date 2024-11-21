package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentState
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.OffenderStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.STAFF_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDate

class OffenderServiceTest {
  private val assessmentRepository = mock<AssessmentRepository>()
  private val offenderRepository = mock<OffenderRepository>()
  private val prisonerSearchService = mock<PrisonerSearchService>()
  private val probationService = mock<ProbationService>()
  private val staffRepository = mock<StaffRepository>()
  private val telemetryClient = mock<TelemetryClient>()

  private val service: OffenderService =
    OffenderService(
      assessmentRepository,
      offenderRepository,
      prisonerSearchService,
      probationService,
      staffRepository,
      telemetryClient,
    )

  @Test
  fun `should get the case admin case load`() {
    val offender1 = anOffender()
    val offender2 =
      offender1.copy(id = offender1.id + 1, bookingId = offender1.bookingId + 29, hdced = offender1.hdced.plusWeeks(12))
    whenever(offenderRepository.findByPrisonIdAndStatus(PRISON_ID, OffenderStatus.NOT_STARTED)).thenReturn(
      listOf(
        offender1,
        offender2,
      ),
    )

    val caseload = service.getCaseAdminCaseload(PRISON_ID)
    assertThat(caseload.size).isEqualTo(2)
    assertThat(caseload.map { it.bookingId }).containsExactlyInAnyOrder(offender1.bookingId, offender2.bookingId)
  }

  @Test
  fun `should get the com case load`() {
    val assessment1 =
      anOffender().currentAssessment().copy(status = AssessmentState.AddressAndRiskChecksInProgress)
    val assessment2 = anOffender().currentAssessment().copy(status = AssessmentState.AwaitingAddressAndRiskChecks)
    whenever(
      assessmentRepository.findByResponsibleComStaffIdentifierAndStatusIn(
        STAFF_ID,
        listOf(AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS, AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS),
      ),
    ).thenReturn(listOf(assessment1, assessment2))

    val caseload = service.getComCaseload(STAFF_ID)
    assertThat(caseload.size).isEqualTo(2)
    assertThat(caseload.map { it.bookingId }).containsExactlyInAnyOrder(
      assessment1.offender.bookingId,
      assessment2.offender.bookingId,
    )
  }

  @Test
  fun `should create a new offender for a prisoner that has an HDCED`() {
    val hdced = LocalDate.now().plusDays(6)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, hdced))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
    assertThat(offenderCaptor.value.assessments.first().policyVersion).isEqualTo(PolicyService.CURRENT_POLICY_VERSION.code)
  }

  @Test
  fun `should create a new offender and create responsible com where it doesn't already exist`() {
    val hdced = LocalDate.now().plusDays(23)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    val offenderManager = aDeliusOffenderManager()
    whenever(probationService.getCurrentResponsibleOfficer(PRISON_NUMBER)).thenReturn(offenderManager)

    val communityOffenderManager = aCommunityOffenderManager(offenderManager)
    whenever(staffRepository.save(any())).thenReturn(communityOffenderManager)

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val communityOffenderManagerCaptor = ArgumentCaptor.forClass(CommunityOffenderManager::class.java)
    verify(staffRepository).save(communityOffenderManagerCaptor.capture())
    assertThat(communityOffenderManagerCaptor.value)
      .extracting("staffIdentifier", "username", "email", "forename", "surname")
      .isEqualTo(
        listOf(
          communityOffenderManager.staffIdentifier,
          communityOffenderManager.username,
          communityOffenderManager.email,
          communityOffenderManager.forename,
          communityOffenderManager.surname,
        ),
      )

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, hdced))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
    assertThat(offenderCaptor.value.assessments.first().policyVersion).isEqualTo(PolicyService.CURRENT_POLICY_VERSION.code)
  }

  @Test
  fun `should create a new offender and assign responsible com where it already exists`() {
    val hdced = LocalDate.now().plusDays(19)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    val offenderManager = aDeliusOffenderManager()
    whenever(probationService.getCurrentResponsibleOfficer(PRISON_NUMBER)).thenReturn(offenderManager)

    val communityOffenderManager = aCommunityOffenderManager(offenderManager)
    whenever(staffRepository.findByStaffIdentifier(offenderManager.id)).thenReturn(communityOffenderManager)

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, hdced))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
    assertThat(offenderCaptor.value.assessments.first().policyVersion).isEqualTo(PolicyService.CURRENT_POLICY_VERSION.code)

    verify(staffRepository, never()).save(any())
  }

  @Test
  fun `should not create a new offender for a prisoner that does not have an HDCED`() {
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner()
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository, never()).findByPrisonNumber(PRISON_NUMBER)
  }

  @Test
  fun `should update an existing offender for a prisoner that has an HDCED`() {
    val existingHdced = LocalDate.now().plusDays(6)
    val updatedHdced = LocalDate.now().plusDays(10)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = updatedHdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(existingHdced),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, updatedHdced))
  }

  @Test
  fun `should not update an existing offender if hdced or names haven't changed`() {
    val hdced = LocalDate.now().plusDays(28)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(hdced),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    verify(offenderRepository, never()).save(any())
  }

  @Test
  fun `should throw an exception when the the offender cannot be found in prisoner search`() {
    val exception = assertThrows<Exception> { service.createOrUpdateOffender(PRISON_NUMBER) }
    assertThat(exception.message).isEqualTo("Could not find prisoner with prisonNumber $PRISON_NUMBER in prisoner search")
  }
}
