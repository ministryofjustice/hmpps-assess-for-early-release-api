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
  fun `should search for prisoners by nomis ids`() {
    val nomisId1 = "Z1234XY"
    val nomisId2 = "H4784GE"
    val nomisIds = listOf(nomisId1, nomisId2)

    val prisonerSearchPrisoners =
      listOf(
        PrisonerSearchPrisoner(prisonerNumber = nomisId1, firstName = "firstname1", lastName = "lastname1", dateOfBirth = LocalDate.of(1981, 5, 23)),
        PrisonerSearchPrisoner(prisonerNumber = nomisId2, firstName = "firstname2", lastName = "lastname2", dateOfBirth = LocalDate.of(1998, 3, 30)),
      )
    whenever(prisonerSearchApiClient.searchPrisonersByNomisIds(nomisIds)).thenReturn(prisonerSearchPrisoners)

    val prisoners = prisonerSearchService.searchPrisonersByNomisIds(nomisIds)

    verify(prisonerSearchApiClient).searchPrisonersByNomisIds(nomisIds)
    assertThat(prisoners.size).isEqualTo(prisonerSearchPrisoners.size)
    assertThat(prisoners[0].prisonerNumber).isEqualTo(nomisId1)
    assertThat(prisoners[1].prisonerNumber).isEqualTo(nomisId2)
  }
}
