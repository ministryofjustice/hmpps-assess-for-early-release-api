package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.entity.events.AssessmentEventType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentEventResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.AssessmentSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.support.OffenderSearchResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.SupportService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class SupportResource(
  private val agentHolder: AgentHolder,
  private val supportService: SupportService,
) {

  @GetMapping("/support/offender/search/{searchString}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the offenders details for the given search string",
    description = "Returns the offenders details for the give search string (prison or crn number)",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returned the offender for the given search string",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = OffenderSearchResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun searchForOffender(@Parameter(required = true) @PathVariable @Size(max = 10, min = 4) searchString: String) = supportService.searchForOffender(searchString)

  @GetMapping("/support/offender/{prisonNumber}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the offender for the give prison number",
    description = "Returns the offender for the give prison number)",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returned the offender for the given prison number",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = OffenderResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getOffender(@Parameter(required = true) @PathVariable @Size(min = 4) prisonNumber: String) = supportService.getOffender(prisonNumber)

  @GetMapping("/support/offender/assessment/{assessmentId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the assessment for a given assessment ID",
    description = "Returns the assessment for a given assessment ID",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returned the assessment for a given assessment ID",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AssessmentResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "Could not find an assessment for given assessment id",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getAssessment(@Parameter(required = true) @PathVariable @Positive assessmentId: Long) = supportService.getAssessment(assessmentId)

  @GetMapping("/support/offender/{prisonNumber}/assessments")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the assessments for the given prisoner",
    description = "Returns the assessments for the given prisoner",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returned the assessments for the given prisoner",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = AssessmentSearchResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getAssessments(@Parameter(required = true) @PathVariable prisonNumber: String) = supportService.getAssessments(prisonNumber)

  @DeleteMapping("/support/offender/{prisonNumber}/assessment/current")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Deletes the current assessment for a prisoner",
    description = "Deletes details of the current assessment for a prisoner",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Returns No Content status code",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteCurrentAssessment(@Parameter(required = true) @PathVariable prisonNumber: String) {
    supportService.deleteCurrentAssessment(prisonNumber, agentHolder.agent)
  }

  @DeleteMapping("/support/offender/assessment/{assessmentId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Deletes the current assessment for the given id",
    description = "Deletes the current assessment for the given id",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "Returns No Content status code",
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteAssessment(@Parameter(required = true) @PathVariable assessmentId: Long) {
    supportService.deleteAssessment(assessmentId, agentHolder.agent)
  }

  @GetMapping("/support/offender/assessment/{assessmentId}/events")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.OK)
  @Operation(
    summary = "Gets the assessment events for the given id and filter",
    description = "Gets the assessment events for the given id and filter",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the assessments events",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = AssessmentEventResponse::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getEvents(
    @Parameter(required = true) @PathVariable assessmentId: Long,
    @RequestParam(required = false, name = "filter") filter: List<AssessmentEventType>?,
  ): List<AssessmentEventResponse> = supportService.getAssessmentEvents(assessmentId, filter)
}
