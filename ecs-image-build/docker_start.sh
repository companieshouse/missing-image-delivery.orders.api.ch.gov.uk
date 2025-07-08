#!/bin/bash

# Start script for missing-image-delivery.orders.api.ch.gov.uk

PORT=8080

exec java -jar -Dserver.port="${PORT}" -XX:MaxRAMPercentage=80 "missing-image-delivery.orders.api.ch.gov.uk.jar"
