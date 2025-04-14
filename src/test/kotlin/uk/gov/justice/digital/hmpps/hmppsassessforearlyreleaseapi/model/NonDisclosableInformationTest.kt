package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model

import jakarta.validation.ConstraintValidatorContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class NonDisclosableInformationTest {

  private lateinit var validator: NonDisclosableInformationValidator
  private lateinit var context: ConstraintValidatorContext

  @BeforeEach
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    validator = NonDisclosableInformationValidator()
    context = Mockito.mock(ConstraintValidatorContext::class.java)
  }

  @Test
  fun `should be valid when hasNonDisclosableInformation is false`() {
    val info = NonDisclosableInformation(hasNonDisclosableInformation = false, nonDisclosableInformation = null)
    assertThat(validator.isValid(info, context)).isTrue()
  }

  @Test
  fun `should be valid when hasNonDisclosableInformation is true and nonDisclosableInformation is not blank`() {
    val info = NonDisclosableInformation(hasNonDisclosableInformation = true, nonDisclosableInformation = "Some reason")
    assertThat(validator.isValid(info, context)).isTrue()
  }

  @Test
  fun `should be invalid when hasNonDisclosableInformation is true and nonDisclosableInformation is blank`() {
    val info = NonDisclosableInformation(hasNonDisclosableInformation = true, nonDisclosableInformation = "")
    assertThat(validator.isValid(info, context)).isFalse()
  }

  @Test
  fun `should be invalid when hasNonDisclosableInformation is true and nonDisclosableInformation is null`() {
    val info = NonDisclosableInformation(hasNonDisclosableInformation = true, nonDisclosableInformation = null)
    assertThat(validator.isValid(info, context)).isFalse()
  }
}
