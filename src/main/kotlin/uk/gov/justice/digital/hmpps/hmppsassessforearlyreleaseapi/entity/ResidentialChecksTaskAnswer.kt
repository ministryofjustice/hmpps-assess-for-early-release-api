package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Column
import jakarta.persistence.DiscriminatorColumn
import jakarta.persistence.DiscriminatorType
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
import java.time.LocalDateTime

enum class ResidentialChecksTaskAnswerType {
  ADDRESS_DETAILS_AND_INFORMED_CONSENT,
  POLICE_CHECK,
  CHILDREN_SERVICES_CHECK,
  ASSESS_THIS_PERSONS_RISK,
  SUITABILITY_DECISION,
  MAKE_A_RISK_MANAGEMENT_DECISION,
}

@Entity
@Table(name = "residential_checks_task_answer")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "answer_type", discriminatorType = DiscriminatorType.STRING)
abstract class ResidentialChecksTaskAnswer(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long = -1,

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "address_check_request_id", nullable = false)
  val addressCheckRequest: CurfewAddressCheckRequest,

  @NotNull
  @Column(name = "task_code")
  val taskCode: String,

  @NotNull
  @Column(name = "task_version")
  val taskVersion: String,

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(name = "answer_type", insertable = false, updatable = false)
  val answerType: ResidentialChecksTaskAnswerType,

  @NotNull
  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  @NotNull
  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)

sealed interface AnswerPayload {
  fun createTaskAnswersEntity(addressCheckRequest: CurfewAddressCheckRequest, taskVersion: String): ResidentialChecksTaskAnswer
}
