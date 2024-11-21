package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import com.tinder.StateMachine.Transition.Invalid
import com.tinder.StateMachine.Transition.Valid
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
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

  @ManyToOne(optional = false)
  @JoinColumn(name = "offender_id", nullable = false)
  val offender: Offender,

  @NotNull
  @Embedded
  @AttributeOverrides(
    AttributeOverride(name = "status", column = Column(name = "status")),
    AttributeOverride(name = "previousStatus", column = Column(name = "previous_status")),
  )
  var status: AssessmentState = AssessmentState.NotStarted,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  var lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val policyVersion: String = "???",

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  val eligibilityCheckResults: MutableSet<EligibilityCheckResult> = mutableSetOf(),

  @OneToMany(mappedBy = "assessment", fetch = FetchType.LAZY, cascade = [CascadeType.ALL], orphanRemoval = true)
  @OrderBy("eventTime DESC")
  val assessmentEvents: MutableList<AssessmentEvent> = mutableListOf(),

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "responsible_com_id")
  var responsibleCom: CommunityOffenderManager? = null,
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

  private fun Any.label() = this::class.simpleName

  fun performTransition(
    event: AssessmentLifecycleEvent,
  ): AssessmentState {
    val currentStatus = this.status
    val transition = assessmentStateMachine.with { initialState(currentStatus) }.transition(event)
    return when (transition) {
      is Invalid -> {
        error("Fail to transition Assessment: '${this.id}', triggered by '${transition.event.label()}' from '${transition.fromState.label()}'")
      }

      is Valid -> {
        log.info("Transitioned Assessment: '${this.id}', triggered by event: '${transition.event.label()}' from '${transition.fromState.label()}' to '${transition.toState.label()}'")
        if (currentStatus != transition.fromState) {
          assessmentEvents.add(
            StatusChangedEvent(
              assessment = this,
              changes = StatusChange(before = transition.fromState.label, after = transition.toState.label),
            ),
          )
          status = transition.toState
          lastUpdatedTimestamp = LocalDateTime.now()
        }
        transition.toState
      }
    }
  }
}
