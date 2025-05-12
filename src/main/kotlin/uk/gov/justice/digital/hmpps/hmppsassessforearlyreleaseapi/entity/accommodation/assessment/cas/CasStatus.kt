package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas

enum class CasStatus {
  PROPOSED,
  PERSON_INELIGIBLE,
  PERSON_ELIGIBLE,
  REFERRAL_REQUESTED,
  REFERRAL_ACCEPTED,
  REFERRAL_REFUSED,
  REFERRAL_WITHDRAWN,
  ADDRESS_PROVIDED,
  ;

  fun getValidTo(status: CasStatus): CasStatus {
    if (isValidTo(status)) {
      return status
    }
    throw IllegalStateException("Fail to transition CAS status from ${this.name} to ${status.name}  ")
  }

  fun isValidTo(status: CasStatus): Boolean = getValidOptionsForStatus().contains(status)

  fun getValidOptionsForStatus(): Set<CasStatus> {
    val validOptions = when (this) {
      PROPOSED -> setOf(PERSON_INELIGIBLE, PERSON_ELIGIBLE)
      PERSON_ELIGIBLE -> setOf(REFERRAL_REQUESTED)
      REFERRAL_REQUESTED -> setOf(REFERRAL_ACCEPTED, REFERRAL_REFUSED, REFERRAL_WITHDRAWN)
      REFERRAL_ACCEPTED -> setOf(ADDRESS_PROVIDED)
      PERSON_INELIGIBLE, REFERRAL_REFUSED,
      REFERRAL_WITHDRAWN, ADDRESS_PROVIDED,
      -> setOf()
    }
    return validOptions
  }
}
