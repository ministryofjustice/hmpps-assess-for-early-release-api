package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentEventType.STATUS_CHANGE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.toEntity

@Entity
@DiscriminatorValue(value = "STATUS_CHANGE")
class StatusChangedEvent(
  id: Long = -1L,
  assessment: Assessment,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val changes: StatusChange,

  agent: Agent = Agent.SYSTEM_AGENT.toEntity(),
) : AssessmentEvent(
  id = id,
  assessment = assessment,
  eventType = STATUS_CHANGE,
  summary = "status changed from: '${changes.before}', to: '${changes.after}'",
  agent = agent,
) {

  override fun toString(): String = "StatusChangedEvent(" +
    "id=$id, " +
    "assessment=${assessment.id}, " +
    "summary=$summary, " +
    "changes=$changes, " +
    ")"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is StatusChangedEvent) return false
    if (!super.equals(other)) return false
    return true
  }

  override fun hashCode(): Int = super.hashCode()
}

data class StatusChange(val before: AssessmentStatus, val after: AssessmentStatus, val context: Map<String, Any>)
