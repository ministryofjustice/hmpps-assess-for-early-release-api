package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service

@Service
class PrisonerSearchService(
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  fun searchPrisonersByNomisIds(nomisIds: List<String>): List<PrisonerSearchPrisoner> {
    if (nomisIds.isEmpty()) return emptyList()
    return prisonerSearchApiClient.searchPrisonersByNomisIds(nomisIds)
  }
}
