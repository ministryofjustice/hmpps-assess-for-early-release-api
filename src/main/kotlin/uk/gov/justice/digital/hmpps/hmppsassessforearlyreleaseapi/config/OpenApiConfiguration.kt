package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config

import io.swagger.v3.core.util.PrimitiveType
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.DateTimeSchema
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.media.StringSchema
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.customizers.OperationCustomizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.info.BuildProperties
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.expression.BeanFactoryResolver
import org.springframework.expression.spel.SpelEvaluationException
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.method.HandlerMethod

@Configuration
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  @Autowired
  private lateinit var context: ApplicationContext

  @Bean
  fun customOpenAPI(): OpenAPI = OpenAPI().servers(
    listOf(
      Server().url("https://assess-for-early-release-api-dev.hmpps.service.justice.gov.uk")
        .description("Development"),
      Server().url("http://localhost:8089").description("Local"),
    ),
  ).info(
    Info().title("Assess for early release API").version(version)
      .description("API for the Assess for early release product. NB: Not intended for general use")
      .contact(Contact().name("HMPPS Digital Studio").email("feedback@digital.justice.gov.uk")),
  ).components(
    Components().addSecuritySchemes(
      "assess-for-early-release-admin-role",
      SecurityScheme().addBearerJwtRequirement("ROLE_ASSESS_FOR_EARLY_RELEASE_ADMIN"),
    ),
  ).addSecurityItem(SecurityRequirement().addList("assess-for-early-release-admin-role", listOf("read", "write")))

  private fun SecurityScheme.addBearerJwtRequirement(role: String): SecurityScheme = type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT").`in`(SecurityScheme.In.HEADER)
    .name("Authorization").description("A HMPPS Auth access token with the `$role` role.")

  @Bean
  fun preAuthorizeCustomizer(): OperationCustomizer = OperationCustomizer { operation: Operation, handlerMethod: HandlerMethod ->
    // Get PreAuthorize for method or fallback to class annotation
    (
      handlerMethod.getMethodAnnotation(PreAuthorize::class.java)?.value ?: handlerMethod.beanType.getAnnotation(
        PreAuthorize::class.java,
      )?.value
      )?.let {
      val preAuthExp = SpelExpressionParser().parseExpression(it)
      val spelEvalContext = StandardEvaluationContext()
      spelEvalContext.beanResolver = BeanFactoryResolver(context)
      spelEvalContext.setRootObject(
        object {
          fun hasRole(role: String) = listOf(role)
          fun hasAnyRole(vararg roles: String) = roles.toList()
        },
      )

      val roles = try {
        (preAuthExp.getValue(spelEvalContext) as List<*>).asListOfType<String>()
      } catch (e: SpelEvaluationException) {
        log.warn("Failed to process SPEL fragment", e)
        emptyList()
      }

      if (roles.isNotEmpty()) {
        operation.description =
          "${operation.description ?: ""}\n\n" + "Requires one of the following roles:\n" + roles.joinToString(
            prefix = "* ",
            separator = "\n* ",
          )
      }
    }
    operation
  }

  @Bean
  fun openAPICustomizer(): OpenApiCustomizer = OpenApiCustomizer {
    val mapSchema = Schema<Any>().apply {
      type = "object"
      additionalProperties = Schema<Any>().apply {
        oneOf = listOf(Schema<Any>().apply { type = "string" }, Schema<Any>().apply { type = "boolean" })
      }
    }
    it.components.addSchemas("MapStringAny", mapSchema)

    val problemDetailSchema = Schema<Any>().apply {
      type = "object"
      properties = mapOf(
        "type" to Schema<Any>().apply { type = "string" },
        "title" to Schema<Any>().apply { type = "string" },
        "status" to Schema<Any>().apply { type = "integer" },
        "detail" to Schema<Any>().apply { type = "string" },
        "instance" to Schema<Any>().apply { type = "string" },
      )
      additionalProperties = Schema<Any>().apply {
        oneOf = listOf(Schema<Any>().apply { type = "string" }, Schema<Any>().apply { type = "number" }, Schema<Any>().apply { type = "boolean" })
      }
    }
    it.components.addSchemas("ProblemDetail", problemDetailSchema)

    it.components.schemas.forEach { (_, schema: Schema<*>) ->
      val properties = schema.properties ?: mutableMapOf()
      for (propertyName in properties.keys) {
        val propertySchema = properties[propertyName]!!
        if (propertySchema is DateTimeSchema) {
          properties.replace(
            propertyName,
            StringSchema().example("2021-07-05T10:35:17").pattern("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$")
              .description(propertySchema.description).required(propertySchema.required),
          )
        }
      }
    }
  }.also {
    PrimitiveType.enablePartialTime() // Prevents generation of a LocalTime schema which causes conflicts with java.time.LocalTime
  }

  companion object {
    val log: Logger = LoggerFactory.getLogger(this::class.java)
  }
}

/**
 * Conversion is not guaranteed, only use when you know (or at least expect) all types to be the same type.
 */
@Suppress("UNCHECKED_CAST")
inline fun <reified T> List<*>.asListOfType() = this as List<T>
