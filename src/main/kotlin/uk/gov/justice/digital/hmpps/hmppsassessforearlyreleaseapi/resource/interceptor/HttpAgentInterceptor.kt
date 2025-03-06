package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto


@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class AgentHolder {
  lateinit var agent: AgentDto
}

@Configuration
class HeaderInterceptorConfig(private val agentHolder: AgentHolder) : WebMvcConfigurer {
  override fun addInterceptors(registry: InterceptorRegistry) {
    registry.addInterceptor(agentInterceptor())
  }

  @Bean
  fun agentInterceptor(): AgentInterceptor {
    return AgentInterceptor(agentHolder)
  }
}

class AgentInterceptor(private val agentHolder: AgentHolder) : HandlerInterceptor {

  @Throws(Exception::class)
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    var role = request.getHeader("role")
    agentHolder.agent = AgentDto(
      username = request.getHeader("username") ?: "SYSTEM",
      fullName = request.getHeader("fullName") ?: "SYSTEM",
      role = if(role != null) UserRole.valueOf(role) else UserRole.SYSTEM,
      onBehalfOf = request.getHeader("onBehalfOf") ?: "SYSTEM",
    )
    return true
  }
}
