package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.PRISON_NAME
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aPrisonApiUserDetails
import java.time.LocalDate

class PrisonServiceTest {
  private val prisonApiClient = mock<PrisonApiClient>()
  private val prisonRegisterApiClient = mock<PrisonRegisterApiClient>()
  private val prisonerSearchApiClient = mock<PrisonerSearchApiClient>()

  private val prisonService = PrisonService(prisonApiClient, prisonRegisterApiClient, prisonerSearchApiClient)

  @Test
  fun `should get user details`() {
    val userDetails = aPrisonApiUserDetails()
    whenever(prisonApiClient.getUserDetails(userDetails.username)).thenReturn(userDetails)
    val result = prisonService.getUserDetails(userDetails.username)
    assertThat(result).isEqualTo(userDetails)
    verify(prisonApiClient).getUserDetails(userDetails.username)
  }

  @Test
  fun `should return empty list if given no nomis ids`() {
    val prisoners = prisonService.searchPrisonersByNomisIds(emptyList())
    assertTrue(prisoners.isEmpty())
    verify(prisonerSearchApiClient, never()).searchPrisonersByNomisIds(any())
  }

  @Test
  fun `should batch search for prisoners by nomis ids`() {
    val batchSize = 500
    val nomisIds = Array(batchSize * 2 + 1) { index -> "Z" + String.format("%03d", index) + "XY" }.asList()

    val prisonerSearchPrisoners = nomisIds.mapIndexed { index, nomisId ->
      PrisonerSearchPrisoner(
        prisonerNumber = nomisId,
        firstName = "firstname: $index",
        lastName = "lastname1",
        dateOfBirth = LocalDate.of(1981, 5, 23),
        cellLocation = "A-1-002",
        mostSeriousOffence = "Robbery",
        prisonName = PRISON_NAME,
      )
    }

    val prisoners1 = prisonerSearchPrisoners.slice(0..<batchSize)
    val prisoners2 = prisonerSearchPrisoners.slice(batchSize..<batchSize * 2)
    val prisoners3 = listOf(prisonerSearchPrisoners.last())
    whenever(prisonerSearchApiClient.searchPrisonersByNomisIds(any())).thenReturn(prisoners1, prisoners2, prisoners3)

    val prisoners = prisonService.searchPrisonersByNomisIds(nomisIds)

    assertThat(prisoners).hasSize(batchSize * 2 + 1)
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(nomisIds.slice(0..<batchSize))
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(nomisIds.slice(batchSize..<batchSize * 2))
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(listOf(nomisIds.last()))
  }

  @Test
  fun `should get a map of prison ids to names`() {
    whenever(prisonRegisterApiClient.getPrisons()).thenReturn(
      listOf(
        Prison("BMI", "Birmingham (HMP)"),
        Prison("LCI", "Leicester (HMP)"),
        Prison("WEI", "Wealstun (HMP)"),
      ),
    )

    val prisonIdNameMap = prisonService.getPrisonIdsAndNames()
    assertThat(prisonIdNameMap).isEqualTo(
      mapOf(
        "BMI" to "Birmingham (HMP)",
        "LCI" to "Leicester (HMP)",
        "WEI" to "Wealstun (HMP)",
      ),
    )
  }
}
