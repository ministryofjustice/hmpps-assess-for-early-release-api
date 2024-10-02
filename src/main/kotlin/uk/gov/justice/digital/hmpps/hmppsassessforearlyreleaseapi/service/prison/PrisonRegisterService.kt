package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison

import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class PrisonRegisterService(
  private val prisonRegisterApiClient: PrisonRegisterApiClient,
) {
  fun getPrisonIdsAndNames(): Map<String, String> {
    val prisons = prisonRegisterApiClient.getPrisons()
    return prisons.associate { it.prisonId to it.prisonName }
  }

  fun getNameForId(id: String): String {
    val prisons = getPrisonIdsAndNames()
    return prisons[id] ?: throw EntityNotFoundException("Cannot find a prison with prison id in prison register: $id")
  }
}
