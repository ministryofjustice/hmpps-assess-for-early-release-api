# hmpps-assess-for-early-release-api
[![repo standards badge](https://img.shields.io/badge/endpoint.svg?&style=flat&logo=github&url=https%3A%2F%2Foperations-engineering-reports.cloud-platform.service.justice.gov.uk%2Fapi%2Fv1%2Fcompliant_public_repositories%2Fhmpps-assess-for-early-release-api)](https://operations-engineering-reports.cloud-platform.service.justice.gov.uk/public-report/hmpps-assess-for-early-release-api "Link to report")
[![CircleCI](https://circleci.com/gh/ministryofjustice/hmpps-assess-for-early-release-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/hmpps-assess-for-early-release-api)
[![Docker Repository on Quay](https://img.shields.io/badge/quay.io-repository-2496ED.svg?logo=docker)](https://quay.io/repository/hmpps/hmpps-assess-for-early-release-api)
[![API docs](https://img.shields.io/badge/API_docs_-view-85EA2D.svg?logo=swagger)](https://assess-for-early-release-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html)

This is the backend API for Assess for Early Release (Afer).

# Checking changes before pushing

```
./gradlew clean ktlintformat detekt test integrationtest
```

Note: this uses test containers to start a postgres instance on a random port to run integration tests against. 

If you'd prefer to run tests against a postgres instance running externally via docker-compose, then you can start an instance using:
```
docker compose -f docker-compose-test.yml up
```
The tests should automatically detect this running on port 5433 and use it instead. 

### Running application on command line

```
./run-local.sh
```

### Running application inside the IDE

This option is needed if you want to trace or debug your code!
If you want to run the spring application in side your IDE, 
follow the following steps:

* Run in terminal
```
./set-vars-to-env-file.sh
```
* Then run in terminal
```
docker compose up
```
* Add the following to your IJ IDE Run/Debug configurations "Environment variables"
```
/Users/<<YOUR-USER-DIR>>/env-config/after.env
```
<em>The IDE behaves a bit odd here, try and selected the folder and file do not type it in!</em>
### Running all tests from the command line
```
./gradlew clean ktlintformat detekt test integrationtest
```