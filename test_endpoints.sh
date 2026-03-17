#!/bin/bash

# Get list of programs and tariffs
curl -X GET http://localhost:8080/api/programs

# Create an application (replace programId and tariffId with actual ones from the GET request)
echo "\n"
curl -X POST http://localhost:8080/api/applications \
-H "Content-Type: application/json" \
-d '{
  "programId": 1,
  "tariffId": 1,
  "userEmail": "student@example.com",
  "userName": "John Doe"
}'

# Simulate successful bank webhook for created application (replace applicationId with actual id)
echo "\n"
curl -X POST http://localhost:8080/api/payments/webhook \
-H "Content-Type: application/json" \
-d '{
  "applicationId": 1,
  "status": "SUCCESS"
}'