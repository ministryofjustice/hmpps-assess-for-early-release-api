package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus

@Repository
interface AssessmentRepository : JpaRepository<Assessment, Long> {

  fun findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(prisonNumber: String): List<Assessment>

  fun findByResponsibleComStaffCodeAndStatusInAndDeletedTimestampIsNull(staffCode: String, status: List<AssessmentStatus>): List<Assessment>

  fun findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonId: String): List<Assessment>

  fun findByResponsibleComStaffCodeAndDeletedTimestampIsNull(staffCode: String): List<Assessment>
}
