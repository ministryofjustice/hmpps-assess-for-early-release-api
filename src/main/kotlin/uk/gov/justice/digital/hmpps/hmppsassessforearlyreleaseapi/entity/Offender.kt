package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.state.AssessmentStatus
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "offender")
data class Offender(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @NotNull
  val bookingId: Long,

  @NotNull
  val prisonNumber: String,

  val prisonId: String,

  val forename: String? = null,

  val surname: String? = null,

  @NotNull
  val dateOfBirth: LocalDate,

  @NotNull
  val hdced: LocalDate,

  val crd: LocalDate? = null,

  val crn: String? = null,

  val sentenceStartDate: LocalDate? = null,

  @OneToMany(mappedBy = "offender", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val assessments: MutableSet<Assessment> = mutableSetOf(),

  @NotNull
  @Enumerated(EnumType.STRING)
  val status: AssessmentStatus = AssessmentStatus.NOT_STARTED,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) {

  fun currentAssessment(): Assessment = this.assessments.first()

  @Override
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other == null || Hibernate.getClass(this) != Hibernate.getClass(other)) return false
    other as Offender

    return id == other.id
  }

  @Override
  override fun hashCode(): Int = javaClass.hashCode()

  @Override
  override fun toString() = this::class.simpleName + "(id: $id, status: $status)"
}
