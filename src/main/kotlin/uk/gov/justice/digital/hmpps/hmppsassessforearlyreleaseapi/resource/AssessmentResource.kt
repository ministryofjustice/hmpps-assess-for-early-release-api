package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import jakarta.validation.Valid
import jakarta.validation.ValidationException
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AssessmentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutReasonType
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.OptOutRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AssessmentService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AssessmentResource(
  private val assessmentService: AssessmentService,
) {

  @GetMapping("/offender/{prisonNumber}/current-assessment")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the current assessment for a prisoner",
    description = "Returns details of the current assessment for a prisoner",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the current assessment for the prisoner",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AssessmentSummary::class),
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
  fun getCurrentAssessment(@Parameter(required = true) @PathVariable prisonNumber: String) =
    assessmentService.getCurrentAssessmentSummary(prisonNumber)

  @PutMapping("/offender/{prisonNumber}/current-assessment/opt-out")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Opts an offender out of being assessed for early release.",
    description = "Opts an offender out of being assessed for early release.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The offender has been opted out of assess for early release.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Void::class)),
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
  fun optOut(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Valid @RequestBody optOutRequest: OptOutRequest,
  ) {
    if (optOutRequest.reasonType == OptOutReasonType.OTHER && optOutRequest.otherDescription.isNullOrBlank()) {
      throw ValidationException("otherDescription cannot be blank if reasonType is OTHER")
    }
    assessmentService.optOut(prisonNumber)
  }

  @PutMapping("/offender/{prisonNumber}/current-assessment/opt-in")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Allows an offender to opt back in to being assessed for early release.",
    description = "Allows an offender to opt back in to being assessed for early release.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The offender has been opted back into being assessed for early release.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Void::class)),
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
  fun optBackIn(
    @Parameter(required = true) @PathVariable prisonNumber: String,
  ) {
    assessmentService.optBackIn(prisonNumber)
  }

  @PutMapping("/offender/{prisonNumber}/current-assessment/submit-for-address-checks")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Submits an offender's current assessment for address checks.",
    description = "Submits an offender's current assessment so that address checks by the probation practitioner can begin.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The offender's current assessment has been submitted for address checks.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Void::class)),
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
        description = "Could not find an offender with the provided prison number",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun submitForAddressChecks(@Parameter(required = true) @PathVariable prisonNumber: String) =
    assessmentService.submitAssessmentForAddressChecks(prisonNumber)
}