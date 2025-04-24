package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.Hibernate
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
  val prisonNumber: String,

  val prisonId: String,

  var forename: String? = null,

  var surname: String? = null,

  @NotNull
  var dateOfBirth: LocalDate,

  var crd: LocalDate? = null,

  val crn: String? = null,

  var sentenceStartDate: LocalDate? = null,

  @OneToMany(mappedBy = "offender", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @OrderBy("id")
  val assessments: MutableSet<Assessment> = mutableSetOf(),

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  var lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
) {

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
  override fun toString() = this::class.simpleName + "(id: $id, assessments: $assessments)"
}
