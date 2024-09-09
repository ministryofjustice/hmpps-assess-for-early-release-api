package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.OffenderStatus

@Repository
interface OffenderRepository : JpaRepository<Offender, Long> {
  fun findByPrisonNumber(prisonerNumber: String): Offender?

  fun findByPrisonIdAndStatus(prisonId: String, status: OffenderStatus): List<Offender>
}
