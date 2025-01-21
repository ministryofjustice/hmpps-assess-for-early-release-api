package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.residentialChecks

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.TestData.aRiskManagementDecisionTaskAnswers
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.policy.model.residentialchecks.TaskStatus

class ResidentialChecksTaskAnswerTest {
  @Test
  fun `should get task status`() {
    val suitableRiskManagementDecisionAnswers = aRiskManagementDecisionTaskAnswers(
      criterionMet = true,
    )
    assertThat(suitableRiskManagementDecisionAnswers.status()).isEqualTo(TaskStatus.SUITABLE)

    val unsuitableRiskManagementDecisionAnswers = aRiskManagementDecisionTaskAnswers(
      criterionMet = false,
    )
    assertThat(unsuitableRiskManagementDecisionAnswers.status()).isEqualTo(TaskStatus.UNSUITABLE)
    assertThat(null.status()).isEqualTo(TaskStatus.NOT_STARTED)
  }
}
