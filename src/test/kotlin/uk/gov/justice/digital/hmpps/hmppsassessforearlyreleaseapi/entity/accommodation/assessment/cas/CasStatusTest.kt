package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.accommodation.assessment.cas

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class CasStatusTest {

  @ParameterizedTest(name = "Does allow POSTPONED to {0}")
  @EnumSource(
    names = [
      "PERSON_INELIGIBLE", "PERSON_ELIGIBLE",
    ],
  )
  fun `should allow valid PROPOSED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.PROPOSED.isValidTo(toState)

    // Then
    assertThat(isValid).isTrue()
  }

  @ParameterizedTest(name = "Does not allow POSTPONED to {0}")
  @EnumSource(
    names = [
      "PROPOSED", "REFERRAL_REQUESTED", "REFERRAL_ACCEPTED", "REFERRAL_REFUSED", "REFERRAL_WITHDRAWN", "ADDRESS_PROVIDED",
    ],
  )
  fun `should not allow valid PROPOSED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.PROPOSED.isValidTo(toState)

    // Then
    assertThat(isValid).isFalse()
  }

  @ParameterizedTest(name = "Does allow PERSON_ELIGIBLE to {0}")
  @EnumSource(
    names = [
      "REFERRAL_REQUESTED",
    ],
  )
  fun `should allow valid PERSON_ELIGIBLE to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.PERSON_ELIGIBLE.isValidTo(toState)

    // Then
    assertThat(isValid).isTrue()
  }

  @ParameterizedTest(name = "Does not allow PERSON_ELIGIBLE to {0}")
  @EnumSource(
    names = [
      "PROPOSED", "PERSON_INELIGIBLE", "PERSON_ELIGIBLE", "REFERRAL_ACCEPTED", "REFERRAL_REFUSED", "REFERRAL_WITHDRAWN", "ADDRESS_PROVIDED",
    ],
  )
  fun `should not allow valid PERSON_ELIGIBLE to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.PERSON_ELIGIBLE.isValidTo(toState)

    // Then
    assertThat(isValid).isFalse()
  }

  @ParameterizedTest(name = "Does allow REFERRAL_REQUESTED to {0}")
  @EnumSource(
    names = [
      "REFERRAL_ACCEPTED", "REFERRAL_REFUSED", "REFERRAL_WITHDRAWN",
    ],
  )
  fun `should allow valid REFERRAL_REQUESTED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.REFERRAL_REQUESTED.isValidTo(toState)

    // Then
    assertThat(isValid).isTrue()
  }

  @ParameterizedTest(name = "Does not allow REFERRAL_REQUESTED to {0}")
  @EnumSource(
    names = [
      "PROPOSED", "PERSON_INELIGIBLE", "PERSON_ELIGIBLE", "REFERRAL_REQUESTED", "ADDRESS_PROVIDED",
    ],
  )
  fun `should not allow valid REFERRAL_REQUESTED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.REFERRAL_REQUESTED.isValidTo(toState)

    // Then
    assertThat(isValid).isFalse()
  }

  @ParameterizedTest(name = "Does allow REFERRAL_ACCEPTED to {0}")
  @EnumSource(
    names = [
      "ADDRESS_PROVIDED",
    ],
  )
  fun `should allow valid REFERRAL_ACCEPTED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.REFERRAL_ACCEPTED.isValidTo(toState)

    // Then
    assertThat(isValid).isTrue()
  }

  @ParameterizedTest(name = "Does not allow REFERRAL_ACCEPTED to {0}")
  @EnumSource(
    names = [
      "PROPOSED", "PERSON_INELIGIBLE", "PERSON_ELIGIBLE", "REFERRAL_REQUESTED", "REFERRAL_ACCEPTED", "REFERRAL_REFUSED", "REFERRAL_WITHDRAWN",
    ],
  )
  fun `should not allow valid REFERRAL_ACCEPTED to given state`(toState: CasStatus) {
    // When
    val isValid = CasStatus.REFERRAL_ACCEPTED.isValidTo(toState)

    // Then
    assertThat(isValid).isFalse()
  }

  @ParameterizedTest(name = "This status {0} as no to state")
  @EnumSource(
    names = [
      "PERSON_INELIGIBLE", "REFERRAL_REFUSED", "REFERRAL_WITHDRAWN", "ADDRESS_PROVIDED",
    ],
  )
  fun `states should have no to state`(state: CasStatus) {
    // Given

    // When
    val states = state.getValidOptionsForStatus()

    // Then
    assertThat(states).isEmpty()
  }

  @Test
  fun `When invalid to state then exception is thrown`() {
    assertThrows<IllegalStateException> {
      CasStatus.PROPOSED.getValidTo(CasStatus.ADDRESS_PROVIDED)
    }
  }
}
