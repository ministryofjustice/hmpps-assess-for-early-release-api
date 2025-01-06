package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
@EnableWebMvc
class WebConfiguration : WebMvcConfigurer {

  override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
    registry.addResourceHandler("/resources/**")
      .addResourceLocations("/resources/")
//      .setCacheControl(CacheControl.maxAge(Duration.ofDays(365)))
  }
}