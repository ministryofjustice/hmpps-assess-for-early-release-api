package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.Type
import java.time.LocalDateTime

enum class CriterionType {
  ELIGIBILITY,
  SUITABILITY,
}

@Entity
@Table(name = "eligibility_check_result")
data class EligibilityCheckResult(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @ManyToOne
  @JoinColumn(name = "assessment_id", nullable = false)
  val assessment: Assessment,

  @NotNull
  val criterionMet: Boolean,

  @NotNull
  @Enumerated(EnumType.STRING)
  val criterionType: CriterionType,

  @NotNull
  val criterionCode: String,

  @NotNull
  val criterionVersion: String,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  @Type(JsonBinaryType::class)
  @Column(columnDefinition = "jsonb")
  val questionAnswers: Map<String, Boolean>,
) {
  @Override
  override fun toString(): String =
    this::class.simpleName + "(id: $id, criterionMet: $criterionMet, questionAnswers: $questionAnswers)"

  @Override
  override fun hashCode(): Int = javaClass.hashCode()
}
