package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorValue
import jakarta.persistence.Entity
import org.hibernate.annotations.Type

@Entity
@DiscriminatorValue(value = "GENERIC_EVENT")
class GenericChangedEvent(
  id: Long = -1L,
  assessment: Assessment,

  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val changes: Map<String, Any>,

  eventType: AssessmentEventType,
  agent: Agent = Agent(UserRole.SYSTEM.name, UserRole.SYSTEM.name, UserRole.SYSTEM, UserRole.SYSTEM.name),
) : AssessmentEvent(
  id = id,
  assessment = assessment,
  eventType = eventType,
  summary = "generic change event with type: $eventType",
  agent = agent,
) {

  override fun toString(): String = "GenericChangedEvent(" +
    "id=$id, " +
    "assessment=${assessment.id}, " +
    "eventType=$eventType, " +
    "summary=$summary, " +
    "changes=$changes, " +
    ")"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is GenericChangedEvent) return false
    if (!super.equals(other)) return false
    return true
  }

  override fun hashCode(): Int = super.hashCode()
}
