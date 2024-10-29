package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ADDRESS_AND_RISK_CHECKS_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.ELIGIBLE_AND_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.INELIGIBLE_OR_UNSUITABLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.NOT_STARTED
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.AssessmentStatus.OPTED_OUT
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.ELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityStatus.INELIGIBLE
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService.AssessmentWithEligibilityProgress
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithChecksComplete
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.anAssessmentWithEligibilityProgress

class AssessmentLifecycleServiceTest {
  val assessmentLifecycleService = AssessmentLifecycleService()

  @Nested
  inner class `eligibility answer submitted` {
    @Test
    fun `checks failed`() {
      val assessment =
        anAssessmentWithEligibilityProgress().copy(
          eligibilityProgress = {
            anAssessmentWithEligibilityProgress().eligibilityProgress().map {
              it.copy(
                status = INELIGIBLE,
              )
            }
          },
        )

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(INELIGIBLE_OR_UNSUITABLE)
    }
  }

  @Test
  fun `checks in progress`() {
    val assessment =
      anAssessmentWithEligibilityProgress().copy(
        eligibilityProgress = {
          anAssessmentWithEligibilityProgress().eligibilityProgress().mapIndexed { i, it ->
            if (i == 0) {
              it.copy(
                status = ELIGIBLE,
              )
            } else {
              it
            }
          }
        },
      )

    val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

    assertThat(newStatus).isEqualTo(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
  }

  @Nested
  inner class `eligibility checks complete` {
    lateinit var assessment: AssessmentWithEligibilityProgress

    @BeforeEach
    fun setup() {
      assessment = anAssessmentWithChecksComplete()
    }

    @Test
    fun `from NOT_STARTED`() {
      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `from ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS`() {
      assessment.assessmentEntity.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `from INELIGIBLE_OR_UNSUITABLE`() {
      assessment.assessmentEntity.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(INELIGIBLE_OR_UNSUITABLE)

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `from INELIGIBLE_OR_UNSUITABLE after previously been eligible`() {
      assessment.assessmentEntity.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)
      assessment.assessmentEntity.changeStatus(INELIGIBLE_OR_UNSUITABLE)

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `from ADDRESS_AND_RISK_CHECKS_IN_PROGRESS after previously been eligible`() {
      assessment.assessmentEntity.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)
      assessment.assessmentEntity.changeStatus(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(INELIGIBLE_OR_UNSUITABLE)

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    }

    @Test
    fun `more complicated example`() {
      assessment.assessmentEntity.changeStatus(ELIGIBILITY_AND_SUITABILITY_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)
      assessment.assessmentEntity.changeStatus(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(INELIGIBLE_OR_UNSUITABLE)
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)
      assessment.assessmentEntity.changeStatus(OPTED_OUT)
      assessment.assessmentEntity.changeStatus(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
      assessment.assessmentEntity.changeStatus(INELIGIBLE_OR_UNSUITABLE)

      val newStatus = assessmentLifecycleService.eligibilityAnswerSubmitted(assessment)

      assertThat(newStatus).isEqualTo(ADDRESS_AND_RISK_CHECKS_IN_PROGRESS)
    }
  }

  @Nested
  inner class `Submit Assessment for address checks` {
    lateinit var assessment: AssessmentWithEligibilityProgress

    @BeforeEach
    fun setup() {
      assessment = anAssessmentWithChecksComplete()
    }

    @Test
    fun `submit an assessment where checks are complete`() {
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)

      val newStatus = assessmentLifecycleService.submitAssessmentForAddressChecks(assessment)

      assertThat(newStatus).isEqualTo(AssessmentStatus.AWAITING_ADDRESS_AND_RISK_CHECKS)
    }

    @Test
    fun `submit an assessment where checks are incomplete`() {
      assessment.assessmentEntity.changeStatus(NOT_STARTED)

      assertThrows<IllegalStateException> {
        assessmentLifecycleService.submitAssessmentForAddressChecks(assessment)
      }
    }
  }

  @Nested
  inner class `Opt out of Assessment` {
    lateinit var assessment: AssessmentWithEligibilityProgress

    @BeforeEach
    fun setup() {
      assessment = anAssessmentWithChecksComplete()
    }

    @Test
    fun `opt out of an assessment`() {
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)

      val newStatus = assessmentLifecycleService.optOut(assessment)

      assertThat(newStatus).isEqualTo(OPTED_OUT)
    }

    @Test
    fun `opt out when already opted out`() {
      assessment.assessmentEntity.changeStatus(OPTED_OUT)

      assertThrows<IllegalStateException> {
        assessmentLifecycleService.optOut(assessment)
      }
    }
  }

  @Nested
  inner class `Opt in to an Assessment` {
    lateinit var assessment: AssessmentWithEligibilityProgress

    @BeforeEach
    fun setup() {
      assessment = anAssessmentWithChecksComplete()
    }

    @Test
    fun `opt in to an assessment`() {
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)
      assessment.assessmentEntity.changeStatus(OPTED_OUT)

      val newStatus = assessmentLifecycleService.optBackIn(assessment)

      assertThat(newStatus).isEqualTo(ELIGIBLE_AND_SUITABLE)
    }

    @Test
    fun `opt in to an assessment when not opted out`() {
      assessment.assessmentEntity.changeStatus(ELIGIBLE_AND_SUITABLE)

      assertThrows<IllegalStateException> {
        assessmentLifecycleService.optBackIn(assessment)
      }
    }
  }
}
