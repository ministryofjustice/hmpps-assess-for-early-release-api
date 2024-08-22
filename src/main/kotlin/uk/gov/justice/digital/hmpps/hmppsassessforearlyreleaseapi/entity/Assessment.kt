package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

@Entity
@Table(name = "assessment")
data class Assessment(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @NotNull
  val id: Long,

  val bookingId: Long,

  val prisonerNumber: String,

  val prisonId: String? = null,

  val createdTimestamp: LocalDateTime = LocalDateTime.now(),

  val lastUpdatedTimestamp: LocalDateTime = LocalDateTime.now(),
)
