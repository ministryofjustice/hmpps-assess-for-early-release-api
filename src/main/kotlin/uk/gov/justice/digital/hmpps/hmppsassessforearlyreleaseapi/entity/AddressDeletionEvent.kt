package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEvent
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.AddressDeleteReasonType

@Entity
@Table(name = "address_deletion_event")
data class AddressDeletionEvent(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @Enumerated(EnumType.STRING)
  var reasonType: AddressDeleteReasonType? = null,

  var otherReason: String? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  @JoinColumn(name = "assessment_event_id", referencedColumnName = "id")
  val assessmentEvent: AssessmentEvent? = null,
) {
  override fun toString(): String = "AddressDeletionEvent(" +
    "id=$id, " +
    "reasonType=$reasonType, " +
    "otherReason=$otherReason, " +
    "assessmentEvent=${assessmentEvent?.id}" +
    ")"

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    if (!super.equals(other)) return false
    return true
  }

  override fun hashCode(): Int = super.hashCode()
}
