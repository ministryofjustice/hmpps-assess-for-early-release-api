package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorType
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.DiscriminatorFormula
import java.time.LocalDateTime
import java.util.Objects

enum class AssessmentEventType {
  STATUS_CHANGE,
  RESIDENT_UPDATED,
  ADDRESS_UPDATED,
}

@Entity
@Table(name = "assessment_event")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula(
  "CASE WHEN event_type = 'STATUS_CHANGE' THEN 'STATUS_CHANGE' " +
    " ELSE 'GENERIC_EVENT' END",
  discriminatorType = DiscriminatorType.STRING,
)
abstract class AssessmentEvent(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long? = -1,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assessment_id", nullable = true)
  val assessment: Assessment,

  @Enumerated(EnumType.STRING)
  @Column(name = "event_type")
  var eventType: AssessmentEventType,

  @Embedded
  val agent: Agent,

  val eventTime: LocalDateTime = LocalDateTime.now(),

  val summary: String? = null,
) {

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is AssessmentEvent) return false
    if (id != other.id) return false
    return true
  }

  override fun hashCode(): Int = Objects.hash(id)
}
