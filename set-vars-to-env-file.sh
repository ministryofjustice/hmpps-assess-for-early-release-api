#
# This script is used to create an .env file to allow us to set env vars to allow us to run the spring app locally in the IDE.
# run this script file in a terminal as follows  ./set-vars-to-local-env.sh
#
# Add the following to your IJ IDE Run/Debug configurations "Environment variables"
#    /Users/<<YOUR-USER-DIR>>/env-config/after.env
#
# and click Run (use must have docker running)
#
set -e
export SERVER_PORT=8089
# Match with the credentials set in docker-compose.yml
export DB_SERVER=localhost
export DB_NAME=assess-for-early-release-db
export DB_USER=afer
export DB_PASS=afer
export OS_PLACES_API_KEY=$(kubectl -n hmpps-assess-for-early-release-dev get secrets hmpps-assess-for-early-release-api -o json  | jq -r '.data.OS_PLACES_API_KEY | @base64d')
export SYSTEM_CLIENT_ID=$(kubectl -n hmpps-assess-for-early-release-dev get secrets hmpps-assess-for-early-release-api -o json  | jq -r '.data.SYSTEM_CLIENT_ID | @base64d')
export SYSTEM_CLIENT_SECRET=$(kubectl -n hmpps-assess-for-early-release-dev get secrets hmpps-assess-for-early-release-api -o json  | jq -r '.data.SYSTEM_CLIENT_SECRET | @base64d')

# Provide URLs to other local container-based dependent services
# Match with ports defined in docker-compose.yml
export HMPPS_AUTH_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth

# Make the connection without specifying the sslmode=verify-full requirement
export SPRING_DATASOURCE_URL='jdbc:postgresql://${DB_SERVER}/${DB_NAME}'

fileDir=~/env-config/
mkdir -p $fileDir
cd $fileDir
fileToAddVars='afer.env'
rm -f $fileToAddVars 2> /dev/null
echo "SERVER_PORT=$SERVER_PORT" >> $fileToAddVars
echo "DB_SERVER=$DB_SERVER" >> $fileToAddVars
echo "DB_NAME=$DB_NAME" >> $fileToAddVars
echo "DB_USER=$DB_USER" >> $fileToAddVars
echo "DB_PASS=$DB_PASS" >> $fileToAddVars
echo "OS_PLACES_API_KEY=$OS_PLACES_API_KEY" >> $fileToAddVars
echo "SYSTEM_CLIENT_ID=$SYSTEM_CLIENT_ID" >> $fileToAddVars
echo "SYSTEM_CLIENT_SECRET=$SYSTEM_CLIENT_SECRET" >> $fileToAddVars
echo "HMPPS_AUTH_URL=$HMPPS_AUTH_URL" >> $fileToAddVars
echo "SPRING_DATASOURCE_URL=$SPRING_DATASOURCE_URL" >> $fileToAddVars