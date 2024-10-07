package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.ELIGIBILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CriterionType.SUITABILITY
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.POLICY_1_0
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.Criterion

@Service
class PolicyService {
  private val policyVersions = mapOf(
    POLICY_1_0.code to POLICY_1_0,
  )

  fun getVersionFromPolicy(version: String) = policyVersions[version] ?: error("Unrecognised version: $version")

  fun getCriterion(version: String, type: CriterionType, code: String): Criterion {
    val policy = policyVersions[version] ?: error("Unrecognised version: $version")
    val criteria = when (type) {
      SUITABILITY -> policy.suitabilityCriteria
      ELIGIBILITY -> policy.eligibilityCriteria
    }
    return criteria.find { it.code == code } ?: error("Unrecognised criterion, policy version: $version, type: $type, code: $code")
  }

  companion object {
    val CURRENT_POLICY_VERSION = POLICY_1_0
  }
}
