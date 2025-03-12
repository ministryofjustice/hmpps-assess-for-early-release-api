package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEvent

@Repository
interface AssessmentEventRepository : JpaRepository<AssessmentEvent, Long> {

  fun findByAssessmentId(assessmentId: Long): List<AssessmentEvent>
}
