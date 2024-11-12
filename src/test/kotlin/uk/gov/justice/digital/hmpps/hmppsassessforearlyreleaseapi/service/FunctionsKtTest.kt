package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class FunctionsKtTest {

  @Test
  fun `should convert a string of words to title case`() {
    assertThat("".convertToTitleCase()).isEqualTo("")
    assertThat("word".convertToTitleCase()).isEqualTo("Word")
    assertThat("two words".convertToTitleCase()).isEqualTo("Two Words")
    assertThat("UPPER CASE WORDS".convertToTitleCase()).isEqualTo("Upper Case Words")
  }
}
