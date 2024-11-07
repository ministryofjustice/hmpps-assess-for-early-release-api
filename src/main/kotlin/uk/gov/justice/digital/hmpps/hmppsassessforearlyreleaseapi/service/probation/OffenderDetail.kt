package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.probation

data class OffenderDetail(
  val offenderId: Long,
  val otherIds: OtherIds,
  val offenderManagers: List<OffenderManager>?,
)

data class OtherIds(
  val crn: String,
  val croNumber: String? = null,
  val pncNumber: String? = null,
  val nomsNumber: String? = null,
)

data class OffenderManager(
  val staff: StaffHuman,
  val active: Boolean? = null,
)

data class StaffHuman(
  val code: String? = null,
  val forenames: String? = null,
  val surname: String? = null,
  val unallocated: Boolean? = null,
)
