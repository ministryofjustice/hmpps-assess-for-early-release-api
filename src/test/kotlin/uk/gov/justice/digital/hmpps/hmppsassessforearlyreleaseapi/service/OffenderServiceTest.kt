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
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository.OffenderRepository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchService
import java.time.LocalDate

class OffenderServiceTest {

  private val offenderRepository = mock<OffenderRepository>()
  private val prisonerSearchService = mock<PrisonerSearchService>()
  private val telemetryClient = mock<TelemetryClient>()

  private val service: OffenderService = OffenderService(offenderRepository, prisonerSearchService, telemetryClient)

  @Test
  fun `should create a new offender for a prisoner that has an HDCED`() {
    val hdced = LocalDate.now().plusDays(6)
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    service.createOrUpdateOffender(PRISONER_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))
    verify(offenderRepository).findByPrisonerNumber(PRISONER_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonerNumber", "bookingId", "firstName", "lastName", "hdced")
      .isEqualTo(listOf(PRISONER_NUMBER, BOOKING_ID.toLong(), FIRST_NAME, LAST_NAME, hdced))
  }

  @Test
  fun `should not create a new offender for a prisoner that does not have an HDCED`() {
    val prisonerSearchPrisoner = aPrisonerSearchPrisoner()
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )

    service.createOrUpdateOffender(PRISONER_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))
    verify(offenderRepository, never()).findByPrisonerNumber(PRISONER_NUMBER)
  }

  @Test
  fun `should update an existing offender for a prisoner that has an HDCED`() {
    val existingHdced = LocalDate.now().plusDays(6)
    val updatedHdced = LocalDate.now().plusDays(10)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced = updatedHdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)).thenReturn(
      Offender(
        id = 1,
        bookingId = BOOKING_ID.toLong(),
        prisonerNumber = PRISONER_NUMBER,
        prisonId = PRISON_ID,
        hdced = existingHdced,
      ),
    )

    service.createOrUpdateOffender(PRISONER_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))
    verify(offenderRepository).findByPrisonerNumber(PRISONER_NUMBER)

    val offenderCaptor = ArgumentCaptor.forClass(Offender::class.java)
    verify(offenderRepository).save(offenderCaptor.capture())
    assertThat(offenderCaptor.value)
      .extracting("prisonerNumber", "bookingId", "firstName", "lastName", "hdced")
      .isEqualTo(listOf(PRISONER_NUMBER, BOOKING_ID.toLong(), FIRST_NAME, LAST_NAME, updatedHdced))
  }

  @Test
  fun `should not update an existing offender if hdced or names haven't changed`() {
    val hdced = LocalDate.now().plusDays(28)

    val prisonerSearchPrisoner = aPrisonerSearchPrisoner(hdced)
    whenever(prisonerSearchService.searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))).thenReturn(
      listOf(
        prisonerSearchPrisoner,
      ),
    )
    whenever(offenderRepository.findByPrisonerNumber(PRISONER_NUMBER)).thenReturn(
      Offender(
        id = 1,
        bookingId = BOOKING_ID.toLong(),
        prisonerNumber = PRISONER_NUMBER,
        prisonId = PRISON_ID,
        hdced = hdced,
        firstName = FIRST_NAME,
        lastName = LAST_NAME,
      ),
    )

    service.createOrUpdateOffender(PRISONER_NUMBER)

    verify(prisonerSearchService).searchPrisonersByNomisIds(listOf(PRISONER_NUMBER))
    verify(offenderRepository).findByPrisonerNumber(PRISONER_NUMBER)
    verify(offenderRepository, never()).save(any())
  }

  @Test
  fun `should throw an exception when the the offender cannot be found in prisoner search`() {
    val exception = assertThrows<Exception> { service.createOrUpdateOffender(PRISONER_NUMBER) }
    assertThat(exception.message).isEqualTo("Could not find prisoner with prisonerNumber $PRISONER_NUMBER in prisoner search")
  }

  private fun aPrisonerSearchPrisoner(hdced: LocalDate? = null) = PrisonerSearchPrisoner(
    PRISONER_NUMBER,
    bookingId = BOOKING_ID,
    hdced,
    firstName = FIRST_NAME,
    lastName = LAST_NAME,
    prisonId = PRISON_ID,
  )

  private companion object {
    const val PRISONER_NUMBER = "A1234AA"
    const val BOOKING_ID = "123"
    const val FIRST_NAME = "Bob"
    const val LAST_NAME = "Smith"
    const val PRISON_ID = "AFG"
  }
}
