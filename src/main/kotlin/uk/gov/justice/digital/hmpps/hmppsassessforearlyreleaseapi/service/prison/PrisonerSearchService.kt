package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service

private const val PRISONER_SEARCH_BATCH_SIZE = 500

@Service
class PrisonerSearchService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  fun searchPrisonersByNomisIds(nomisIds: List<String>): List<PrisonerSearchPrisoner> {
    if (nomisIds.isEmpty()) return emptyList()

    val batchedNomisIds = nomisIds.chunked(PRISONER_SEARCH_BATCH_SIZE)
    val batchedPrisoners = batchedNomisIds.map { batch ->
      prisonerSearchApiClient.searchPrisonersByNomisIds(batch)
    }

    return batchedPrisoners.flatten()
  }
}
