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
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.AddressDeleteReasonDto
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddResidentRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddStandardAddressCheckRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.AddressSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.CheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.ResidentSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.StandardAddressCheckRequestSummary
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.model.curfewAddress.UpdateCaseAdminAdditionInfoRequest
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.resource.interceptor.AgentHolder
import uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.service.AddressService

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class AddressResource(private val addressService: AddressService, private val agentHolder: AgentHolder) {

  @GetMapping("/addresses/search/{searchQuery}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Searches for addresses that match the given search text",
    description = "Searches for addresses that match the given search text",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns addresses matching the given search text",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AddressSummary::class),
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
  fun searchForAddresses(@Size(max = 100, min = 5) @PathVariable(name = "searchQuery") searchQuery: String): List<AddressSummary> = addressService.searchForAddresses(searchQuery)

  @GetMapping("/address/uprn/{uprn}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Gets an address by it's UPRN",
    description = "Gets an address by it's UPRN",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the address with the provided UPRN",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = AddressSummary::class),
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
  fun getAddressForUprn(@PathVariable(name = "uprn") uprn: String) = addressService.getAddressForUprn(uprn)

  @PostMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Adds a standard address check request for an offender.",
    description = "Adds a standard address check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The standard address check request has been added to the offender's current assessment.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = StandardAddressCheckRequestSummary::class),
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
  fun addStandardAddressCheckRequest(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @RequestBody @Valid addStandardAddressCheckRequest: AddStandardAddressCheckRequest,
  ) = addressService.addStandardAddressCheckRequest(prisonNumber, addStandardAddressCheckRequest, agentHolder.getAgentOrThrow())

  @GetMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request/{requestId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Gets a standard address check request by it's request id.",
    description = "Gets a standard address check request by it's request id.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Returns the standard address check request with the specified request id",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = StandardAddressCheckRequestSummary::class),
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
        description = "A standard address check request with the specified request id does not exist for the offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getStandardAddressCheckRequest(
    @PathVariable(name = "prisonNumber") prisonNumber: String,
    @PathVariable(name = "requestId") requestId: Long,
  ) = addressService.getStandardAddressCheckRequest(prisonNumber, requestId)

  @DeleteMapping("/offender/{prisonNumber}/current-assessment/address-request/{requestId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Deletes an address check request for an assessment.",
    description = "Deletes an address check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The address check request has been deleted.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = Void::class),
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
        description = "An address check request with the specified id does not exist for the offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun deleteAddressCheckRequest(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
  ) = addressService.deleteAddressCheckRequest(prisonNumber, requestId, agentHolder.getAgentOrThrow())

  @GetMapping("/offender/{prisonNumber}/current-assessment/address-check-requests")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @Operation(
    summary = "Returns check requests that are linked to an offender's current assessment.",
    description = "Returns standard and CAS check requests that are linked to an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "200",
        description = "Address check requests linked to the offender's current assessment are returned",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = CheckRequestSummary::class)),
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
        description = "Not found, an offender with provider number cannot be found",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun getAddressCheckRequestsForAssessment(@Parameter(required = true) @PathVariable prisonNumber: String) = addressService.getCheckRequestsForAssessment(prisonNumber)

  @PostMapping("/offender/{prisonNumber}/current-assessment/standard-address-check-request/{requestId}/resident")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.CREATED)
  @Operation(
    summary = "Adds a resident to standard address check request for an assessment.",
    description = "Adds a resident to a standard address check request for an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "201",
        description = "The resident has been added to the standard address check request.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = ResidentSummary::class)),
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
        description = "A standard address check request with the specified request id does not exist for the offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun addStandardAddressCheckRequestResident(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @RequestBody @Valid addResidentsRequest: List<AddResidentRequest>,
  ) = addressService.addResidents(prisonNumber, requestId, addResidentsRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/{prisonNumber}/current-assessment/address-request/{requestId}/case-admin-additional-information")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Adds case admin additional information to an address.",
    description = "Adds case admin additional information to an address check request.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The case admin additional information has been updated.",
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
        description = "An address check request with the specified id does not exist for the offender",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun updateCaseAdminAdditionalInformation(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @RequestBody @Valid updateCaseAdminAdditionInfoRequest: UpdateCaseAdminAdditionInfoRequest,
  ) = addressService.updateCaseAdminAdditionalInformation(prisonNumber, requestId, updateCaseAdminAdditionInfoRequest, agentHolder.getAgentOrThrow())

  @PutMapping("/offender/{prisonNumber}/current-assessment/withdraw-address/{requestId}")
  @PreAuthorize("hasAnyRole('ASSESS_FOR_EARLY_RELEASE_ADMIN')")
  @ResponseStatus(code = HttpStatus.NO_CONTENT)
  @Operation(
    summary = "Adds a address deletion reason.",
    description = "Adds a reason for deleting an address in the context of an offender's current assessment.",
    security = [SecurityRequirement(name = "assess-for-early-release-admin-role")],
  )
  @ApiResponses(
    value = [
      ApiResponse(
        responseCode = "204",
        description = "The address deletion reason has been added.",
        content = [
          Content(
            mediaType = "application/json",
            array = ArraySchema(schema = Schema(implementation = Void::class)),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "401",
        description = "Unauthorised, requires a valid Oauth2 token.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "403",
        description = "Forbidden, requires an appropriate role.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
      ApiResponse(
        responseCode = "404",
        description = "An address check request with the specified id does not exist for the offender.",
        content = [
          Content(
            mediaType = "application/json",
            schema = Schema(implementation = ErrorResponse::class),
          ),
        ],
      ),
    ],
  )
  fun withdrawAddress(
    @Parameter(required = true) @PathVariable prisonNumber: String,
    @Parameter(required = true) @PathVariable requestId: Long,
    @RequestBody @Valid addressDeleteReason: AddressDeleteReasonDto,
  ) = addressService.withdrawAddress(prisonNumber, requestId, addressDeleteReason, agentHolder.getAgentOrThrow())
}
