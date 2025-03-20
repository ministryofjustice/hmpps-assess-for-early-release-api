package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.helpers

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender

@Repository
interface TestAssessmentRepository : JpaRepository<Assessment, Long> {
  fun findByOffender(offender: Offender): List<Assessment>
  fun findByOffenderPrisonNumber(prisonNumber: String): List<Assessment>
}