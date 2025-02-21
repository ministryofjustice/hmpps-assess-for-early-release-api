package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.exception.ItemNotFoundException

private const val PRISONER_SEARCH_BATCH_SIZE = 500

@Service
class PrisonService(
  private val prisonApiClient: PrisonApiClient,
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
  private val prisonerSearchApiClient: PrisonerSearchApiClient,
) {
  fun getUserDetails(username: String): PrisonApiUserDetail? = prisonApiClient.getUserDetails(username)
    ?: throw ItemNotFoundException("User not found")

  fun searchPrisonersByNomisIds(nomisIds: List<String>): List<PrisonerSearchPrisoner> {
    if (nomisIds.isEmpty()) return emptyList()

    val batchedNomisIds = nomisIds.chunked(PRISONER_SEARCH_BATCH_SIZE)
    val batchedPrisoners = batchedNomisIds.map { batch ->
      prisonerSearchApiClient.searchPrisonersByNomisIds(batch)
    }

    return batchedPrisoners.flatten()
  }

  fun getPrisonIdsAndNames(): Map<String, String> {
    val prisons = prisonRegisterApiClient.getPrisons()
    return prisons.associate { it.prisonId to it.prisonName }
  }

  fun getPrisonNameForId(id: String): String {
    val prisons = getPrisonIdsAndNames()
    return prisons[id] ?: throw ItemNotFoundException("Cannot find a prison with prison id in prison register: $id")
  }
}
