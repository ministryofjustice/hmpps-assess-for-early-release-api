package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentEventType.STATUS_CHANGE

@Entity
@DiscriminatorValue(value = "STATUS_CHANGE")
class StatusChangedEvent(
  id: Long = -1L,
  assessment: Assessment,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val changes: StatusChange,

) : AssessmentEvent(
  id = id,
  assessment = assessment,
  eventType = STATUS_CHANGE,
  summary = "status changed from: '${changes.before}', to: '${changes.after}'",
) {

  override fun toString(): String {
    return "StatusChangedEvent(" +
      "id=$id, " +
      "assessment=${assessment.id}, " +
      "summary=$summary, " +
      "changes=$changes, " +
      ")"
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is StatusChangedEvent) return false
    if (!super.equals(other)) return false
    return true
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}

data class StatusChange(val before: AssessmentStatus, val after: AssessmentStatus)
