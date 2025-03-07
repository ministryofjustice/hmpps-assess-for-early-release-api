package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Component
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AgentDto

@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
class AgentHolder {
  lateinit var agent: AgentDto

  fun isAgentInitialized(): Boolean = this::agent.isInitialized

  fun getAgentOrThrow(): AgentDto {
    if (!isAgentInitialized()) {
      error("Agent is missing from the request headers")
    }
    return agent
  }
}

class AgentHeaderInterceptor(private val agentHolder: AgentHolder) : HandlerInterceptor {

  @Throws(Exception::class)
  override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
    if (shouldSkipRequest(request)) {
      return true
    }

    val username = request.getHeader("username")
    val fullName = request.getHeader("fullName")
    val roleHeader = request.getHeader("role")
    val onBehalfOf = request.getHeader("onBehalfOf")

    if (username != null && fullName != null && roleHeader != null) {
      val role = UserRole.valueOf(roleHeader)
      agentHolder.agent = AgentDto(
        username = username,
        fullName = fullName,
        role = role,
        onBehalfOf = onBehalfOf,
      )
    }
    return true
  }

  private fun shouldSkipRequest(request: HttpServletRequest): Boolean {
    val path = request.requestURI
    return request.method.equals("GET", ignoreCase = true) ||
      path.startsWith("/health") ||
      path.startsWith("/ping") ||
      path.startsWith("/swagger")
  }
}
