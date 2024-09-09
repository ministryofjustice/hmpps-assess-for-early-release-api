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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonRegisterService
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import java.time.LocalDate

class OffenderServiceTest {

  private val offenderRepository = mock<OffenderRepository>()
  private val prisonRegisterService = mock<PrisonRegisterService>()
  private val prisonerSearchService = mock<PrisonerSearchService>()
  private val telemetryClient = mock<TelemetryClient>()

  private val service: OffenderService =
    OffenderService(offenderRepository, prisonRegisterService, prisonerSearchService, telemetryClient)

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
    verify(offenderRepository).findByPrisonerNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonerNumber", "bookingId", "firstName", "lastName", "hdced")
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
    verify(offenderRepository, never()).findByPrisonerNumber(PRISON_NUMBER)
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
    whenever(offenderRepository.findByPrisonerNumber(PRISON_NUMBER)).thenReturn(
      Offender(
        id = 1,
        bookingId = BOOKING_ID.toLong(),
        prisonerNumber = PRISON_NUMBER,
        prisonId = PRISON_ID,
        hdced = existingHdced,
      ),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonerNumber(PRISON_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonerNumber", "bookingId", "firstName", "lastName", "hdced")
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
    whenever(offenderRepository.findByPrisonerNumber(PRISON_NUMBER)).thenReturn(
      Offender(
        id = 1,
        bookingId = BOOKING_ID.toLong(),
        prisonerNumber = PRISON_NUMBER,
        prisonId = PRISON_ID,
        hdced = hdced,
        firstName = FORENAME,
        lastName = SURNAME,
      ),
    )

    service.createOrUpdateOffender(PRISON_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISON_NUMBER))
    verify(offenderRepository).findByPrisonerNumber(PRISON_NUMBER)
    verify(offenderRepository, never()).save(any())
  }

  @Test
  fun `should throw an exception when the the offender cannot be found in prisoner search`() {
    val exception = assertThrows<Exception> { service.createOrUpdateOffender(PRISON_NUMBER) }
    assertThat(exception.message).isEqualTo("Could not find prisoner with prisonerNumber $PRISON_NUMBER in prisoner search")
  }

  @Test
  fun `should get an offenders current assessment`() {
    val hdced = LocalDate.now().plusDays(5)
    val prisonName = "a prison"
    val offender = Offender(
      id = 1,
      bookingId = BOOKING_ID.toLong(),
      prisonerNumber = PRISON_NUMBER,
      prisonId = PRISON_ID,
      firstName = FORENAME,
      lastName = SURNAME,
      hdced = hdced,
    )
    offender.assessments.add(Assessment(offender = offender))
    whenever(offenderRepository.findByPrisonerNumber(PRISON_NUMBER)).thenReturn(offender)
    whenever(prisonRegisterService.getPrisonIdsAndNames()).thenReturn(mapOf(PRISON_ID to prisonName))

    val assessment = service.getCurrentAssessment(PRISON_NUMBER)

    verify(offenderRepository).findByPrisonerNumber(PRISON_NUMBER)
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
