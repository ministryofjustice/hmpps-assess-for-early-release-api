# hmpps-assess-for-early-release-api
[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-assess-for-early-release-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-assess-for-early-release-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-assess-for-early-release-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-assess-for-early-release-api)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/hmpps-assess-for-early-release-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://hmpps-assess-for-early-release-api-dev.hmpps.service.justice.gov.uk/webjars/swagger-ui/index.html?configUrl=/v3/api-docs)

This is the backend API for Assess for Early Release.

# Checking changes before pushing

```
./gradlew clean ktlintformat detekt test integrationtest
```

Note: this uses test containers to start a postgres instance on a random port to run integration tests against. 

If you'd prefer to run tests against a postgres instance running externally via docker-compose, then you can start an instance using:
```
docker-compose -f docker-compose-test.yml up
```
The tests should automatically detect this running on port 5433 and use it instead. 
