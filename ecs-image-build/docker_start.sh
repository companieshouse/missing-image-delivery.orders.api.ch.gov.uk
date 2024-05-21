#!/bin/bash

# Start script for missing-image-delivery.orders.api.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" "missing-image-delivery.orders.api.ch.gov.uk.jar"
