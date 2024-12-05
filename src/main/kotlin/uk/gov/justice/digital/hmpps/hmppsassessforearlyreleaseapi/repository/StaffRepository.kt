package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.CommunityOffenderManager
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Staff

@Repository
interface StaffRepository : JpaRepository<Staff, Long> {
  fun findByStaffCode(staffCode: String): CommunityOffenderManager?

  fun findByStaffCodeOrUsernameIgnoreCase(staffCode: String, username: String): List<CommunityOffenderManager?>
}
