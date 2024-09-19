package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Assessment
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.Offender
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCheckDetails
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.prison.PrisonerSearchPrisoner
import java.time.LocalDate

object TestData {

  const val PRISON_NUMBER = "A1234AA"
  const val BOOKING_ID = "123"
  const val FORENAME = "Bob"
  const val SURNAME = "Smith"
  const val PRISON_ID = "AFG"
  const val PRISON_NAME = "Birmingham (HMP)"

  fun anOffender(hdced: LocalDate = LocalDate.now().plusDays(10)): Offender {
    val offender = Offender(
      id = 1,
      bookingId = BOOKING_ID.toLong(),
      prisonNumber = PRISON_NUMBER,
      prisonId = PRISON_ID,
      forename = FORENAME,
      surname = SURNAME,
      dateOfBirth = LocalDate.of(1981, 5, 23),
      hdced = hdced,
    )
    offender.assessments.add(Assessment(offender = offender))
    return offender
  }

  fun aPrisonerSearchPrisoner(hdced: LocalDate? = null) = PrisonerSearchPrisoner(
    PRISON_NUMBER,
    bookingId = BOOKING_ID,
    hdced,
    firstName = FORENAME,
    lastName = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonId = PRISON_ID,
  )

  fun anAssessmentSummary() = AssessmentSummary(
    forename = FORENAME,
    surname = SURNAME,
    dateOfBirth = LocalDate.of(1981, 5, 23),
    prisonNumber = PRISON_NUMBER,
    hdced = LocalDate.of(2020, 10, 25),
    crd = LocalDate.of(2022, 3, 21),
    location = PRISON_NAME,
    status = AssessmentStatus.NOT_STARTED,
    policyVersion = "1.0",
  )

  fun anEligibilityCheckDetails() = EligibilityCheckDetails(
    code = "code-1",
    taskName = "task-1",
    question = "question-1",
    status = EligibilityStatus.ELIGIBLE,
    answer = true,
  )

  fun anSuitabilityCheckDetails() = SuitabilityCheckDetails(
    code = "code-1",
    taskName = "task-1",
    question = "question-1",
    status = SuitabilityStatus.SUITABLE,
    answer = true,
  )
}
