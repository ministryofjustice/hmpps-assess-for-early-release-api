package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.enum.PostponeCaseReasonType
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(name = "postponement_reason")
class PostponementReasonEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @NotNull
  @Enumerated(EnumType.STRING)
  val reasonType: PostponeCaseReasonType,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assessment_id", nullable = false)
  val assessment: Assessment,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),
) {

  @Override
  override fun toString(): String = this::class.simpleName + "(id: $id}"

  @Override
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    if (other is PostponementReasonEntity) {
      return id == other.id
    }
    return false
  }

  override fun hashCode(): Int = Objects.hash(id)
}
