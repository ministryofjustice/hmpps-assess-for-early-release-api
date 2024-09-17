package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class PolicyServiceTest {
  private val service = PolicyService()

  @Test
  fun `getVersionFromPolicy returns existing policy`() {
    val policy = service.getVersionFromPolicy("1.0")
    assertThat(policy.code).isEqualTo("1.0")
  }

  @Test
  fun `getVersionFromPolicy fails to return non-existing policy`() {
    assertThatThrownBy { service.getVersionFromPolicy("unknown policy version") }.hasMessage("Unrecognised version: unknown policy version")
  }
}
