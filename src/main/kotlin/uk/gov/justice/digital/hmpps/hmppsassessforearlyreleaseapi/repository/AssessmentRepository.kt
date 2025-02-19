package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender

@Repository
interface AssessmentRepository : JpaRepository<Assessment, Long> {

  fun findByOffenderPrisonNumber(prisonNumber: String): List<Assessment>

  fun findByOffender(offender: Offender): List<Assessment>

  fun findByOffenderPrisonIdAndStatusIn(prisonId: String, status: List<AssessmentStatus>): List<Assessment>

  fun findByResponsibleComStaffCodeAndStatusIn(staffCode: String, status: List<AssessmentStatus>): List<Assessment>

  fun findAllByOffenderPrisonIdAndStatusIn(prisonId: String, statusCodes: List<AssessmentStatus>): List<Assessment>
}
