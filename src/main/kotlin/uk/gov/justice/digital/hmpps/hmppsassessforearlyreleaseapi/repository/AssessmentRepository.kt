package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus

@Repository
interface AssessmentRepository : JpaRepository<Assessment, Long> {

  fun findByOffenderPrisonNumber(prisonNumber: String): List<Assessment>

  fun findByOffender(offender: Offender): List<Assessment>

  fun findByOffenderPrisonId(prisonId: String): List<Assessment>

  fun findByResponsibleComStaffCodeAndStatusIn(staffCode: String, status: List<AssessmentStatus>): List<Assessment>

  fun findByResponsibleComStaffCode(staffCode: String): List<Assessment>
}
