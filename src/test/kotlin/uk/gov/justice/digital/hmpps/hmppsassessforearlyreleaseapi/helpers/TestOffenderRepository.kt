package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender

@Repository
interface TestOffenderRepository : JpaRepository<Offender, Long> {
  fun findByPrisonNumber(prisonNumber: String): Offender?
  fun findByCrn(prisonNumber: String): Offender?
}
