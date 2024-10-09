package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "assessment")
data class Assessment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @ManyToOne
  @JoinColumn(name = "offender_id", nullable = false)
  val offender: Offender,

  @NotNull
  @Enumerated(EnumType.STRING)
  var status: AssessmentStatus = AssessmentStatus.NOT_STARTED,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val policyVersion: String = "???",

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val eligibilityCheckResults: MutableSet<EligibilityCheckResult> = mutableSetOf(),

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @OrderBy("eventTime DESC")
  val assessmentEvents: MutableList<AssessmentEvent> = mutableListOf(),
) {
  @Override
  override fun toString(): String = this::class.simpleName + "(id: $id, status: $status)"

  fun addOrReplaceEligibilityCriterionResult(
    criterionType: CriterionType,
    criterionCode: String,
    criterionMet: Boolean,
    answers: Map<String, Boolean>,
  ) {
    val currentResults = this.eligibilityCheckResults

    val existingCriterionResult =
      currentResults.find { it.criterionType == criterionType && it.criterionCode == criterionCode }

    val criteria = when {
      existingCriterionResult != null -> {
        currentResults.remove(existingCriterionResult)
        existingCriterionResult.copy(
          criterionMet = criterionMet,
          questionAnswers = answers,
          lastUpdatedTimestamp = LocalDateTime.now(),
        )
      }

      else -> EligibilityCheckResult(
        assessment = this,
        criterionMet = criterionMet,
        questionAnswers = answers,
        criterionCode = criterionCode,
        criterionVersion = this.policyVersion,
        criterionType = criterionType,
      )
    }

    currentResults.add(criteria)
  }

  fun changeStatus(newStatus: AssessmentStatus) {
    if (status != newStatus) {
      assessmentEvents.add(StatusChangedEvent(assessment = this, changes = StatusChange(before = status, after = newStatus)))
      status = newStatus
    }
  }
}
