package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentEventResponse

@Repository
interface AssessmentEventRepository : JpaRepository<AssessmentEvent, Long> {

  fun findByAssessmentId(assessmentId: Long): List<AssessmentEvent>

  @Query(
    "SELECT event_type AS eventType, event_time AS eventTime,summary, username," +
      "            full_name AS fullName, role, on_behalf_of AS onBehalfOf, changes " +
      "FROM assessment_event " +
      "WHERE assessment_id = :assessmentId",
    nativeQuery = true,
  )
  fun findByAssessmentIdOrderByEventTime(assessmentId: Long): List<AssessmentEventResponse>

  @Query(
    "SELECT event_type AS eventType, event_time AS eventTime,summary, username," +
      "            full_name AS fullName, role, on_behalf_of AS onBehalfOf, changes " +
      "FROM assessment_event " +
      "WHERE event_type IN :eventTypeList AND assessment_id = :assessmentId ",
    nativeQuery = true,
  )
  fun findByAssessmentIdAndEventTypeInOrderByEventTime(
    assessmentId: Long,
    eventTypeList: List<String>,
  ): List<AssessmentEventResponse>
}
