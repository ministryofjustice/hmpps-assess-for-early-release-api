package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import com.microsoft.applicationinsights.TelemetryClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.AdditionalAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.FORENAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NUMBER
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.SURNAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aDeliusOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anOffender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation.ProbationService
import java.time.LocalDate

class OffenderServiceTest {
  private val offenderRepository = mock<OffenderRepository>()
  private val prisonService = mock<PrisonService>()
  private val probationService = mock<ProbationService>()
  private val telemetryClient = mock<TelemetryClient>()
  private val assessmentService = mock<AssessmentService>()
  private val assessmentRepository = mock<AssessmentRepository>()

  private val service: OffenderService =
    OffenderService(
      offenderRepository,
      prisonService,
      probationService,
      telemetryClient,
      assessmentService,
      assessmentRepository,
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
    val mockAssessment = mock(Assessment::class.java)
    whenever(offenderRepository.save(any())).then(AdditionalAnswers.returnsFirstArg<Offender>())
    whenever(assessmentService.createAssessment(any(), any(), any(), any())).thenReturn(mockAssessment)

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "forename", "surname", "sentenceStartDate")
      .isEqualTo(listOf(PRISON_NUMBER, FORENAME, SURNAME, sentenceStartDate))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
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

    val mockAssessment = mock(Assessment::class.java)
    whenever(offenderRepository.save(any())).then(AdditionalAnswers.returnsFirstArg<Offender>())
    whenever(assessmentService.createAssessment(any(), any(), any(), any())).thenReturn(mockAssessment)

    // When
    service.createOrUpdateOffender(PRISON_NUMBER)

    // Then
    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "forename", "surname", "crn")
      .isEqualTo(listOf(PRISON_NUMBER, FORENAME, SURNAME, caseReferenceNumber))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
  }

  @Test
  fun `should create a new offender and assign responsible com where it already exists`() {
    // Then
    val hdced = LocalDate.now().plusDays(19)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    val offenderManager = aDeliusOffenderManager()
    whenever(probationService.getCurrentResponsibleOfficer(PRISON_NUMBER)).thenReturn(offenderManager)

    val mockAssessment = mock(Assessment::class.java)
    whenever(assessmentService.createAssessment(any(), any(), any(), any())).thenReturn(mockAssessment)
    whenever(offenderRepository.save(any())).then(AdditionalAnswers.returnsFirstArg<Offender>())

    // When
    service.createOrUpdateOffender(PRISON_NUMBER)

    // Then
    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "forename", "surname")
      .isEqualTo(listOf(PRISON_NUMBER, FORENAME, SURNAME))
    assertThat(offenderCaptor.value.assessments).hasSize(1)
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
    // TODO
    val existingHdced = LocalDate.now().plusDays(6)
    val updatedHdced = LocalDate.now().plusDays(10)
    val anOffender = anOffender(existingHdced)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = updatedHdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender,
    )

    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(anOffender.assessments.first())

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "forename", "surname")
      .isEqualTo(listOf(PRISON_NUMBER, FORENAME, SURNAME))
  }

  @Test
  fun `should not update an existing offender if hdced or names haven't changed`() {
    val hdced = LocalDate.now().plusDays(28)
    val anOffender = anOffender(hdced)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender,
    )
    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(anOffender.assessments.first())

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
    val mockAssessment = mock(Assessment::class.java)
    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(mockAssessment)

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonNumber", "forename", "surname", "sentenceStartDate")
      .isEqualTo(listOf(PRISON_NUMBER, FORENAME, SURNAME, updatedSentenceStartDate))
  }

  @Test
  fun `should not update an existing offender if sentenceStartDate haven't changed`() {
    val hdced = LocalDate.now().plusDays(28)
    val sentenceStartDate = LocalDate.now().plusDays(6)
    val anOffender = anOffender(hdced, sentenceStartDate)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced, sentenceStartDate)
    whenever(prisonService.searchPrisonersByNomisIds(listOf(PRISON_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(
      anOffender,
    )
    whenever(assessmentService.getCurrentAssessment(PRISON_NUMBER)).thenReturn(anOffender.assessments.first())

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
