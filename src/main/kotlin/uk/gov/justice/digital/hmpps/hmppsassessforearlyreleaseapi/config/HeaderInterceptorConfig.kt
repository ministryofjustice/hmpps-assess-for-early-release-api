package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHeaderInterceptor
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder

@Configuration
class HeaderInterceptorConfig(private val agentHolder: AgentHolder) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(agentHeaderInterceptor())
  }

  @Bean
  fun agentHeaderInterceptor(): AgentHeaderInterceptor = AgentHeaderInterceptor(agentHolder)
}
