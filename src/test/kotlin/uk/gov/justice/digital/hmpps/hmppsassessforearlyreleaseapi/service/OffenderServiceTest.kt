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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.staff.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.BOOKING_ID
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aCommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDate

class OffenderServiceTest {
  private val assessmentRepository = mock<AssessmentRepository>()
  private val offenderRepository = mock<OffenderRepository>()
  private val prisonService = mock<PrisonService>()
  private val probationService = mock<ProbationService>()
  private val staffRepository = mock<StaffRepository>()
  private val telemetryClient = mock<TelemetryClient>()

  private val service: OffenderService =
    OffenderService(
      assessmentRepository,
      offenderRepository,
      prisonService,
      probationService,
      staffRepository,
      telemetryClient,
    )

  @Test
  fun `should create a new offender for a prisoner that has an HDCED`() {
    val hdced = LocalDate.now().plusDays(6)
    val sentenceStartDate = LocalDate.now().plusDays(10)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced, sentenceStartDate = sentenceStartDate)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced", "sentenceStartDate")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, hdced, sentenceStartDate))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
    assertThat(offenderCaptor.value.assessments.first().policyVersion).isEqualTo(PolicyService.CURRENT_POLICY_VERSION.code)
  }

  @Test
  fun `should create a new offender and create responsible com where it doesn't already exist`() {
    // Given
    val hdced = LocalDate.now().plusDays(23)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    val caseReferenceNumber = "DX12340A"
    whenever(probationService.getCaseReferenceNumber(PRISON_NUMBER)).thenReturn(caseReferenceNumber)
    val offenderManager = aDeliusOffenderManager()
    whenever(probationService.getCurrentResponsibleOfficer(caseReferenceNumber)).thenReturn(offenderManager)

    val communityOffenderManager = aCommunityOffenderManager(offenderManager)
    whenever(staffRepository.save(any())).thenReturn(communityOffenderManager)

    // When
    service.createOrUpdateOffender(PRISON_NUMBER)

    // Then
    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val communityOffenderManagerCaptor = ArgumentCaptor.forClass(CommunityOffenderManager::class.java)
    verify(staffRepository).save(communityOffenderManagerCaptor.capture())
    assertThat(communityOffenderManagerCaptor.value)
      .extracting("staffCode", "username", "email", "forename", "surname")
      .isEqualTo(
        listOf(
          communityOffenderManager.staffCode,
          communityOffenderManager.username,
          communityOffenderManager.email,
          communityOffenderManager.forename,
          communityOffenderManager.surname,
        ),
      )

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "hdced", "crn")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, hdced, caseReferenceNumber))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
    assertThat(offenderCaptor.value.assessments.first().policyVersion).isEqualTo(PolicyService.CURRENT_POLICY_VERSION.code)
  }

  @Test
  fun `should create a new offender and assign responsible com where it already exists`() {
    val hdced = LocalDate.now().plusDays(19)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    val offenderManager = aDeliusOffenderManager()
    whenever(probationService.getCurrentResponsibleOfficer(PRISON_NUMBER)).thenReturn(offenderManager)

    val communityOffenderManager = aCommunityOffenderManager(offenderManager)
    whenever(staffRepository.findByStaffCode(offenderManager.code)).thenReturn(communityOffenderManager)

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
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
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository, never()).findByPrisonNumber(PRISON_NUMBER)
  }

  @Test
  fun `should update an existing offender for a prisoner that has an HDCED`() {
    val existingHdced = LocalDate.now().plusDays(6)
    val updatedHdced = LocalDate.now().plusDays(10)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = updatedHdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(existingHdced),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
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
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(hdced),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    verify(offenderRepository, never()).save(any())
  }

  @Test
  fun `should update an existing offender for a prisoner that has an sentenceStartDate`() {
    val hdced = LocalDate.now().plusDays(28)
    val existingSentenceStartDate = LocalDate.now().plusDays(6)
    val updatedSentenceStartDate = LocalDate.now().plusDays(10)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced, updatedSentenceStartDate)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(sentenceStartDate = existingSentenceStartDate),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "bookingId", "forename", "surname", "sentenceStartDate")
      .isEqualTo(listOf(PRISON_NUMBER, BOOKING_ID.toLong(), FORENAME, SURNAME, updatedSentenceStartDate))
  }

  @Test
  fun `should not update an existing offender if sentenceStartDate haven't changed`() {
    val hdced = LocalDate.now().plusDays(28)
    val sentenceStartDate = LocalDate.now().plusDays(6)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced, sentenceStartDate)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender(hdced, sentenceStartDate),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    verify(offenderRepository, never()).save(any())
  }

  @Test
  fun `should throw an exception when the the offender cannot be found in prisoner search`() {
    val exception = assertThrows<Exception> { service.createOrUpdateOffender(PRISON_NUMBER) }
    assertThat(exception.message).isEqualTo("Could not find prisoner with prisonNumber $PRISON_NUMBER in prisoner search")
  }
}
