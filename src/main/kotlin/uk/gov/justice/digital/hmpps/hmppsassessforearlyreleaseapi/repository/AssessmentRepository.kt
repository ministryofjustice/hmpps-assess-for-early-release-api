package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentSearchResponse

@Repository
interface AssessmentRepository : JpaRepository<Assessment, Long> {

  fun findByOffenderPrisonNumberAndDeletedTimestampIsNullOrderByCreatedTimestamp(prisonNumber: String): List<Assessment>

  @EntityGraph(value = "Assessment.offender")
  fun findByResponsibleComStaffCodeAndStatusInAndDeletedTimestampIsNull(staffCode: String, status: List<AssessmentStatus>): List<Assessment>

  @EntityGraph(value = "Assessment.offender")
  fun findByOffenderPrisonIdAndDeletedTimestampIsNull(prisonId: String): List<Assessment>

  @EntityGraph(value = "Assessment.offender")
  fun findByOffenderPrisonNumberOrderById(prisonId: String): List<AssessmentSearchResponse>

  @EntityGraph(value = "Assessment.offender")
  fun findByResponsibleComStaffCodeAndDeletedTimestampIsNull(staffCode: String): List<Assessment>

  @EntityGraph(value = "Assessment.offender")
  fun findByTeamCodeInAndDeletedTimestampIsNull(teamCodes: List<String>): List<Assessment>
}
