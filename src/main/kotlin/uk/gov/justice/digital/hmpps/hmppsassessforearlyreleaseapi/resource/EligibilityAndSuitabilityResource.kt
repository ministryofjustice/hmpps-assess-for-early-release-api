package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.CriterionCheck
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityAndSuitabilityCaseView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.EligibilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.SuitabilityCriterionView
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.EligibilityAndSuitabilityService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class EligibilityAndSuitabilityResource(private val eligibilityAndSuitabilityService: EligibilityAndSuitabilityService) {

  @GetMapping("/offender/{prisonNumber}/current-assessment/eligibility-and-suitability")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns the initial checks for a prisoner's current assessment",
    description = "Returns details of the current state of a prisoner's initial checks",
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
            schema = Schema(implementation = EligibilityAndSuitabilityCaseView::class),
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
  fun getCaseView(@Parameter(required = true) @PathVariable prisonNumber: String) = eligibilityAndSuitabilityService.getCaseView(prisonNumber)

  @GetMapping("/offender/{prisonNumber}/current-assessment/eligibility/{code}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns a specific eligibility check for a prisoner's current assessment",
    description = "Returns details of a specific eligibility for a prisoner's initial checks",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns details of a specific eligibility criteria in the current assessment for the prisoner",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = EligibilityCriterionView::class),
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
  fun getEligibilityCriterion(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable code: String,
  ): EligibilityCriterionView = eligibilityAndSuitabilityService.getEligibilityCriterionView(prisonNumber, code)

  @GetMapping("/offender/{prisonNumber}/current-assessment/suitability/{code}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns a specific suitability check for a prisoner's current assessment",
    description = "Returns details of a specific suitability for a prisoner's initial checks",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns details of a specific suitability criteria in the current assessment for the prisoner",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = SuitabilityCriterionView::class),
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
  fun getSuitabilityCheck(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable code: String,
  ): SuitabilityCriterionView = eligibilityAndSuitabilityService.getSuitabilityCriterionView(prisonNumber, code)

  @PutMapping("/offender/{prisonNumber}/current-assessment/eligibility-and-suitability-check")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Sets the state of a current eligbility/suitability check",
    description = "Returns details of a specific suitability for a prisoner's initial checks",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the current eligibility and suitability status for an assessment",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = EligibilityAndSuitabilityCaseView::class),
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
  fun answerCheck(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @Validated @RequestBody answer: CriterionCheck,
  ): EligibilityAndSuitabilityCaseView = eligibilityAndSuitabilityService.saveAnswer(prisonNumber, answer)
}
