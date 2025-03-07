package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import org.springframework.web.servlet.HandlerInterceptor
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.UserRole

class AgentHeaderInterceptorTest {

  private lateinit var agentHolder: AgentHolder
  private lateinit var interceptor: HandlerInterceptor
  private lateinit var request: HttpServletRequest
  private lateinit var response: HttpServletResponse
  private lateinit var handler: Any

  @BeforeEach
  fun setUp() {
    agentHolder = AgentHolder()
    interceptor = AgentHeaderInterceptor(agentHolder)
    request = mock(HttpServletRequest::class.java)
    response = mock(HttpServletResponse::class.java)
    handler = mock(Any::class.java)
  }

  @Test
  fun `should skip GET requests`() {
    whenever(request.method).thenReturn("GET")
    assertThat(interceptor.preHandle(request, response, handler)).isTrue()
  }

  @Test
  fun `should skip health endpoint`() {
    whenever(request.requestURI).thenReturn("/health")
    assertThat(interceptor.preHandle(request, response, handler)).isTrue()
  }

  @Test
  fun `should skip ping endpoint`() {
    whenever(request.requestURI).thenReturn("/ping")
    assertThat(interceptor.preHandle(request, response, handler)).isTrue()
  }

  @Test
  fun `should skip swagger endpoint`() {
    whenever(request.requestURI).thenReturn("/swagger")
    assertThat(interceptor.preHandle(request, response, handler)).isTrue()
  }

  @Test
  fun `should set agent when all headers are present`() {
    whenever(request.getHeader("username")).thenReturn("testUser")
    whenever(request.getHeader("fullName")).thenReturn("Test User")
    whenever(request.getHeader("role")).thenReturn(UserRole.SYSTEM.name)
    whenever(request.getHeader("onBehalfOf")).thenReturn("testBehalf")
    whenever(request.requestURI).thenReturn("/addresses")

    assertThat(interceptor.preHandle(request, response, handler)).isTrue()

    val agent = agentHolder.agent
    assertThat(agent.username == "testUser").isTrue()
    assertThat(agent.fullName == "Test User").isTrue()
    assertThat(agent.role == UserRole.SYSTEM).isTrue()
    assertThat(agent.onBehalfOf == "testBehalf").isTrue()
  }

  @Test
  fun `should not set agent when any header is missing`() {
    whenever(request.getHeader("username")).thenReturn("testUser")
    whenever(request.getHeader("fullName")).thenReturn("Test User")
    whenever(request.getHeader("role")).thenReturn(null)
    whenever(request.getHeader("onBehalfOf")).thenReturn("testBehalf")
    whenever(request.requestURI).thenReturn("/addresses")

    assertThat(interceptor.preHandle(request, response, handler)).isTrue()

    assertThrows<UninitializedPropertyAccessException> {
      agentHolder.agent
    }
  }
}
