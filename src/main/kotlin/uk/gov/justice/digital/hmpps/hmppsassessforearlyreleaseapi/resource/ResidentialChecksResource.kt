package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ProblemDetail
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskAnswersSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksTaskView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.ResidentialChecksView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.residentialChecks.SaveResidentialChecksTaskAnswersRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.ResidentialChecksService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class ResidentialChecksResource(private val residentialChecksService: ResidentialChecksService) {
  @GetMapping("/offender/{prisonNumber}/current-assessment/address-request/{requestId}/residential-checks")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the residential checks for an offender's current assessment",
    description = "Returns details of the current status of the residential checks for an offender's current assessment",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the residential checks for the offender's current assessment",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ResidentialChecksView::class),
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
        description = "An offender with the provided prison number does not exist",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getResidentialChecksView(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
  ) =
    residentialChecksService.getResidentialChecksView(prisonNumber, requestId)

  @GetMapping("/offender/{prisonNumber}/current-assessment/address-request/{requestId}/residential-checks/tasks/{taskCode}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns details of a residential checks task for an address check request",
    description = "Returns details of a residential checks task for an address check request",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns details of the residential check task with the specified code for the specified address check request",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ResidentialChecksTaskView::class),
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
        description = "An address check request with provided id and prison number does not exist",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getResidentialChecksTask(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @Parameter(required = true) @PathVariable taskCode: String,
  ) =
    residentialChecksService.getResidentialChecksTask(prisonNumber, requestId, taskCode)

  @PostMapping("/offender/{prisonNumber}/current-assessment/address-request/{requestId}/residential-checks/answers")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Saves answers for a residential checks task.",
    description = "Save the answers for a residential checks task.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The task answers have been saved.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ResidentialChecksTaskAnswersSummary::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "400",
        description = "The request is invalid, e.g. the answers are not valid for the task",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ProblemDetail::class),
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
        description = "An address check request with the specified request id does not exist for the provided offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun saveResidentialChecksTaskAnswers(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @Valid @RequestBody taskAnswers: SaveResidentialChecksTaskAnswersRequest,
  ): ResidentialChecksTaskAnswersSummary = residentialChecksService.saveResidentialChecksTaskAnswers(prisonNumber, requestId, taskAnswers)
}
