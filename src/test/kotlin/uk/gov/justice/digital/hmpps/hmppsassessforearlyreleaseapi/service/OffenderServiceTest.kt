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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.AssessmentRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import java.time.LocalDate

class OffenderServiceTest {

  private val assessmentRepository = mock<AssessmentRepository>()
  private val offenderRepository = mock<OffenderRepository>()
  private val prisonRegisterService = mock<PrisonRegisterService>()
  private val prisonerSearchService = mock<PrisonerSearchService>()
  private val telemetryClient = mock<TelemetryClient>()

  private val service: OffenderService =
    OffenderService(
      assessmentRepository,
      offenderRepository,
      prisonRegisterService,
      prisonerSearchService,
      telemetryClient,
    )

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

  @Test
  fun `should get an offenders current assessment`() {
    val hdced = LocalDate.now().plusDays(5)
    val prisonName = "a prison"
    val offender = anOffender(hdced)
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonRegisterService.getPrisonIdsAndNames()).thenReturn(mapOf(PRISON_ID to prisonName))

    val assessment = service.getCurrentAssessment(PRISON_NUMBER)

    verify(offenderRepository).findByPrisonNumber(PRISON_NUMBER)
    verify(prisonRegisterService).getPrisonIdsAndNames()
    assertThat(assessment).extracting(
      "forename",
      "surname",
      "prisonNumber",
      "hdced",
      "crd",
      "location",
      "status",
    ).isEqualTo(listOf(FORENAME, SURNAME, PRISON_NUMBER, hdced, null, prisonName, AssessmentStatus.NOT_STARTED))
  }

  @Test
  fun `should opt-out an offender`() {
    val offender = anOffender()
    whenever(offenderRepository.findByPrisonNumber(PRISON_NUMBER)).thenReturn(offender)

    service.optOut(PRISON_NUMBER)

    val assessmentCaptor = ArgumentCaptor.forClass(Assessment::class.java)
    verify(assessmentRepository).save(assessmentCaptor.capture())
    assertThat(assessmentCaptor.value.status).isEqualTo(AssessmentStatus.OPTED_OUT)
  }

  private fun anOffender(hdced: LocalDate = LocalDate.now().plusDays(10)): Offender {
    val offender = Offender(
      id = 1,
      bookingId = BOOKING_ID.toLong(),
      prisonNumber = PRISON_NUMBER,
      prisonId = PRISON_ID,
      forename = FORENAME,
      surname = SURNAME,
      hdced = hdced,
    )
    offender.assessments.add(Assessment(offender = offender))
    return offender
  }

  private fun aPrisonerSearchPrisoner(hdced: LocalDate? = null) = PrisonerSearchPrisoner(
    PRISON_NUMBER,
    bookingId = BOOKING_ID,
    hdced,
    firstName = FORENAME,
    lastName = SURNAME,
    prisonId = PRISON_ID,
  )

  private companion object {
    const val PRISON_NUMBER = "A1234AA"
    const val BOOKING_ID = "123"
    const val FORENAME = "Bob"
    const val SURNAME = "Smith"
    const val PRISON_ID = "AFG"
  }
}
