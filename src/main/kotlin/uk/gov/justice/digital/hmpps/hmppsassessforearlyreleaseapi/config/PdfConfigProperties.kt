package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan

@ConfigurationProperties(prefix = "document.pdf")
@ConfigurationPropertiesScan
data class PdfConfigProperties(
  @NotNull val paperWidth: String,
  @NotNull val paperHeight: String,
  @NotNull val marginTop: String,
  @NotNull val marginBottom: String,
  @NotNull val marginLeft: String,
  @NotNull val marginRight: String,
)
