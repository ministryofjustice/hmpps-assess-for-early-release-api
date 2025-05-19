package uk.gov.justice.digital.hmpps.hmppsassessforearlyreleaseapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.get
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo

private const val OS_PLACES_WIREMOCK_PORT = 8099

class OsPlacesMockServer(private val apiKey: String) : WireMockServer(OS_PLACES_WIREMOCK_PORT) {
  fun stubSearchForAddresses(searchQuery: String) {
    val json = """{
      "header": {
        "uri": "https://api.os.uk/search/places/v1/find?query=$searchQuery",
        "query": "query=$searchQuery",
        "offset": 0,
        "totalresults": 29,
        "format": "JSON",
        "dataset": "DPA",
        "lr": "EN,CY",
        "maxresults": 100,
        "epoch": "112",
        "lastupdate": "2024-09-20",
        "output_srs": "EPSG:27700"
      },
      "results": [
        {
          "DPA": {
            "UPRN": "100120991537",
            "UDPRN": "22659267",
            "ADDRESS": "1, THE STREET, TEST TOWN, TEST CITY, TEST POSTCODE",
            "BUILDING_NUMBER": "1",
            "THOROUGHFARE_NAME": "THE STREET",
            "DEPENDENT_LOCALITY": "TEST TOWN",
            "POST_TOWN": "TEST CITY",
            "POSTCODE": "TEST",
            "RPC": "1",
            "X_COORDINATE": 401024.0,
            "Y_COORDINATE": 154112.0,
            "STATUS": "APPROVED",
            "LOGICAL_STATUS_CODE": "1",
            "CLASSIFICATION_CODE": "RD03",
            "CLASSIFICATION_CODE_DESCRIPTION": "Semi-Detached",
            "LOCAL_CUSTODIAN_CODE": 3940,
            "LOCAL_CUSTODIAN_CODE_DESCRIPTION": "TEST COUNTY",
            "COUNTRY_CODE": "E",
            "COUNTRY_CODE_DESCRIPTION": "This record is within England",
            "POSTAL_ADDRESS_CODE": "D",
            "POSTAL_ADDRESS_CODE_DESCRIPTION": "A record which is linked to PAF",
            "BLPU_STATE_CODE": "2",
            "BLPU_STATE_CODE_DESCRIPTION": "In use",
            "TOPOGRAPHY_LAYER_TOID": "osgb1000015844938",
            "WARD_CODE": "E05013472",
            "PARISH_CODE": "E04011768",
            "LAST_UPDATE_DATE": "01/05/2021",
            "ENTRY_DATE": "15/01/2002",
            "BLPU_STATE_DATE": "12/12/2007",
            "LANGUAGE": "EN",
            "MATCH": 1.0,
            "MATCH_DESCRIPTION": "EXACT",
            "DELIVERY_POINT_SUFFIX": "1A"
          }
        },
        {
          "DPA": {
            "UPRN": "100120991538",
            "UDPRN": "22659277",
            "ADDRESS": "2, A ROAD, TEST TOWN, TEST CITY, TEST POSTCODE",
            "BUILDING_NUMBER": "2",
            "THOROUGHFARE_NAME": "A ROAD",
            "DEPENDENT_LOCALITY": "TEST TOWN",
            "POST_TOWN": "TEST CITY",
            "POSTCODE": "TEST",
            "RPC": "1",
            "X_COORDINATE": 401017.0,
            "Y_COORDINATE": 154112.0,
            "STATUS": "APPROVED",
            "LOGICAL_STATUS_CODE": "1",
            "CLASSIFICATION_CODE": "RD03",
            "CLASSIFICATION_CODE_DESCRIPTION": "Semi-Detached",
            "LOCAL_CUSTODIAN_CODE": 3940,
            "LOCAL_CUSTODIAN_CODE_DESCRIPTION": "TEST COUNTY",
            "COUNTRY_CODE": "E",
            "COUNTRY_CODE_DESCRIPTION": "This record is within England",
            "POSTAL_ADDRESS_CODE": "D",
            "POSTAL_ADDRESS_CODE_DESCRIPTION": "A record which is linked to PAF",
            "BLPU_STATE_CODE": "2",
            "BLPU_STATE_CODE_DESCRIPTION": "In use",
            "TOPOGRAPHY_LAYER_TOID": "osgb1000015844937",
            "WARD_CODE": "E05013472",
            "PARISH_CODE": "E04011768",
            "LAST_UPDATE_DATE": "01/05/2021",
            "ENTRY_DATE": "15/01/2002",
            "BLPU_STATE_DATE": "12/12/2007",
            "LANGUAGE": "EN",
            "MATCH": 1.0,
            "MATCH_DESCRIPTION": "EXACT",
            "DELIVERY_POINT_SUFFIX": "1P"
          }
        },
        {
          "DPA": {
            "UPRN": "100120991539",
            "UDPRN": "22659286",
            "ADDRESS": "3, B ROAD, TEST TOWN, TEST CITY, TEST POSTCODE",
            "BUILDING_NUMBER": "3",
            "THOROUGHFARE_NAME": "PARK ROAD",
            "DEPENDENT_LOCALITY": "TEST TOWN",
            "POST_TOWN": "TEST CITY",
            "POSTCODE": "TEST",
            "RPC": "1",
            "X_COORDINATE": 401003.0,
            "Y_COORDINATE": 154111.0,
            "STATUS": "APPROVED",
            "LOGICAL_STATUS_CODE": "1",
            "CLASSIFICATION_CODE": "RD03",
            "CLASSIFICATION_CODE_DESCRIPTION": "Semi-Detached",
            "LOCAL_CUSTODIAN_CODE": 3940,
            "LOCAL_CUSTODIAN_CODE_DESCRIPTION": "TEST COUNTY",
            "COUNTRY_CODE": "E",
            "COUNTRY_CODE_DESCRIPTION": "This record is within England",
            "POSTAL_ADDRESS_CODE": "D",
            "POSTAL_ADDRESS_CODE_DESCRIPTION": "A record which is linked to PAF",
            "BLPU_STATE_CODE": "2",
            "BLPU_STATE_CODE_DESCRIPTION": "In use",
            "TOPOGRAPHY_LAYER_TOID": "osgb1000015844933",
            "WARD_CODE": "E05013472",
            "PARISH_CODE": "E04011768",
            "LAST_UPDATE_DATE": "01/05/2021",
            "ENTRY_DATE": "15/01/2002",
            "BLPU_STATE_DATE": "12/12/2007",
            "LANGUAGE": "EN",
            "MATCH": 1.0,
            "MATCH_DESCRIPTION": "EXACT",
            "DELIVERY_POINT_SUFFIX": "1Y"
          }
        }
      ]}
    """.trimIndent()

    stubFor(
      get(urlEqualTo("/find?query=$searchQuery&key=$apiKey"))
        .willReturn(
          WireMock.aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            json,
          ).withStatus(200),
        ),
    )
  }

  fun stubSearchForAddressesBadRequest(searchQuery: String) {
    stubFor(
      get(urlEqualTo("/find?query=$searchQuery&key=$apiKey"))
        .willReturn(
          WireMock.aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withStatus(400),
        ),
    )
  }

  fun stubGetAddressByUprn(uprn: String) {
    val json = """
      {
        "header": {
          "uri": "https://api.os.uk/search/places/v1/uprn?uprn=$uprn",
          "query": "uprn=$uprn",
          "offset": 0,
          "totalresults": 1,
          "format": "JSON",
          "dataset": "DPA",
          "lr": "EN,CY",
          "maxresults": 100,
          "epoch": "112",
          "lastupdate": "2024-09-23",
          "output_srs": "EPSG:27700"
        },
        "results": [{
          "DPA": {
            "UPRN": "$uprn",
            "UDPRN": "52126562",
            "ADDRESS": "ORDNANCE SURVEY, 4, TEST DRIVE, OFF TEST ROAD, TEST TOWN, TEST POSTCODE",
            "ORGANISATION_NAME": "ORDNANCE SURVEY",
            "BUILDING_NUMBER": "4",
            "THOROUGHFARE_NAME": "TEST DRIVE",
            "DEPENDENT_LOCALITY": "OFF TEST ROAD",
            "POST_TOWN": "TEST TOWN",
            "POSTCODE": "TEST",
            "RPC": "2",
            "X_COORDINATE": 437292.43,
            "Y_COORDINATE": 115541.95,
            "STATUS": "APPROVED",
            "LOGICAL_STATUS_CODE": "1",
            "CLASSIFICATION_CODE": "CO01GV",
            "CLASSIFICATION_CODE_DESCRIPTION": "Central Government Service",
            "LOCAL_CUSTODIAN_CODE": 1760,
            "LOCAL_CUSTODIAN_CODE_DESCRIPTION": "TEST VALLEY",
            "COUNTRY_CODE": "E",
            "COUNTRY_CODE_DESCRIPTION": "This record is within England",
            "POSTAL_ADDRESS_CODE": "D",
            "POSTAL_ADDRESS_CODE_DESCRIPTION": "A record which is linked to PAF",
            "BLPU_STATE_CODE": "2",
            "BLPU_STATE_CODE_DESCRIPTION": "In use",
            "TOPOGRAPHY_LAYER_TOID": "osgb1000002682081995",
            "WARD_CODE": "E05012936",
            "PARISH_CODE": "E04004629",
            "LAST_UPDATE_DATE": "31/03/2020",
            "ENTRY_DATE": "01/09/2010",
            "BLPU_STATE_DATE": "01/09/2010",
            "LANGUAGE": "EN",
            "MATCH": 1.0,
            "MATCH_DESCRIPTION": "EXACT",
            "DELIVERY_POINT_SUFFIX": "1A"
          }
        }
      ]}
    """.trimIndent()

    stubFor(
      get(urlEqualTo("/uprn?uprn=$uprn&key=$apiKey"))
        .willReturn(
          WireMock.aResponse().withHeader(
            "Content-Type",
            "application/json",
          ).withBody(
            json,
          ).withStatus(200),
        ),
    )
  }
}
