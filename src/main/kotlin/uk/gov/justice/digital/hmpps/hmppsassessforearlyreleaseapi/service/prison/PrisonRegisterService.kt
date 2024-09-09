package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PrisonRegisterService(
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
) {
  @Cacheable("prisons")
  fun getPrisonIdsAndNames(): Map<String, String> {
    val prisons = prisonRegisterApiClient.getPrisons()
    return prisons.associate { it.prisonId to it.prisonName }
  }
}
