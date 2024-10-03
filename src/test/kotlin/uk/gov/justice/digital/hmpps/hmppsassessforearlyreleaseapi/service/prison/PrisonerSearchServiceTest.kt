package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate

class PrisonerSearchServiceTest {
  private val prisonerSearchApiClient = mock<PrisonerSearchApiClient>()

  private val prisonerSearchService = PrisonerSearchService(prisonerSearchApiClient)

  @Test
  fun `should return empty list if given no nomis ids`() {
    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(emptyList())
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
      )
    }

    val prisoners1 = prisonerSearchPrisoners.slice(0..<batchSize)
    val prisoners2 = prisonerSearchPrisoners.slice(batchSize..<batchSize * 2)
    val prisoners3 = listOf(prisonerSearchPrisoners.last())
    whenever(prisonerSearchApiClient.searchPrisonersByNomisIds(any())).thenReturn(prisoners1, prisoners2, prisoners3)

    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(nomisIds)

    assertThat(prisoners).hasSize(batchSize * 2 + 1)
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(nomisIds.slice(0..<batchSize))
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(nomisIds.slice(batchSize..<batchSize * 2))
    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(listOf(nomisIds.last()))
  }
}
