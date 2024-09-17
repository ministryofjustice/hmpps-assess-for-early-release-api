package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0

@Service
class PolicyService {
  private val policyVersions = mapOf(
    POLICY_1_0.code to POLICY_1_0,
  )

  fun getVersionFromPolicy(version: String) = policyVersions[version] ?: error("Unrecognised version: $version")

  companion object {
    val CURRENT_POLICY_VERSION = POLICY_1_0
  }
}
