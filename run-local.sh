#
# This script is used to run the assess for early release API locally with a PostgreSQL container.
#
# It runs with a combination of properties from the default spring profile (in application.yaml) and supplemented
# with the dev profile (from application-dev.yml). The latter overrides some of the defaults.
#
# The environment variables here will also override values supplied in spring profile properties, specifically
# around removing the SSL connection to the database and setting the DB properties, SERVER_PORT and client credentials
# to match those used in the docker-compose files.
#

set -e

restart_docker () {
  # Stop the back end containers
  echo "Bringing down current containers ..."
  docker compose down --remove-orphans

  #Prune existing containers
  #Comment in if you wish to perform a fresh install of all containers where all containers are removed and deleted
  #You will be prompted to continue with the deletion in the terminal
  #docker system prune --all

  echo "Pulling back end containers ..."
  docker compose pull
  docker compose -f docker-compose.yml up -d

  echo "Waiting for back end containers to be ready ..."
  until [ "`docker inspect -f {{.State.Health.Status}} afer-db`" == "healthy" ]; do
      sleep 0.1;
  done;
  until [ "`docker inspect -f {{.State.Health.Status}} localstack-afer-api`" == "healthy" ]; do
      sleep 0.1;
  done;

  echo "Back end containers are now ready"
}

export SERVER_PORT=8089

# Match with the credentials set in docker-compose.yml
export DB_SERVER=localhost
export DB_NAME=assess-for-early-release-db
export DB_USER=afer
export DB_PASS=afer
export OS_PLACES_API_KEY=$(kubectl -n hmpps-assess-for-early-release-dev get secrets hmpps-assess-for-early-release-api -o json  | jq -r '.data.OS_PLACES_API_KEY | @base64d')

# Provide URLs to other local container-based dependent services
# Match with ports defined in docker-compose.yml
export HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth

# Make the connection without specifying the sslmode=verify-full requirement
export SPRING_DATASOURCE_URL='jdbc:postgresql://${DB_SERVER}/${DB_NAME}'

if [[ $1 != "--skip-docker" ]]; then
  restart_docker
fi

# Run the application with stdout and dev profiles active
echo "Starting the API locally"

SPRING_PROFILES_ACTIVE=dev ./gradlew clean bootRun

# End
