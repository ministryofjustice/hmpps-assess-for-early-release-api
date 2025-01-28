package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Resident

@Repository
interface ResidentRepository : JpaRepository<Resident, Long> {
  fun findByStandardAddressCheckRequestId(standardAddressCheckRequestId: Long): List<Resident>
}
