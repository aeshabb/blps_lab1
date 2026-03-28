#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
PROGRAM_ID="${PROGRAM_ID:-1}"
TARIFF_ID="${TARIFF_ID:-1}"
EMAIL="${EMAIL:-student@example.com}"
NAME="${NAME:-John Doe}"
WEBHOOK_STATUS="${WEBHOOK_STATUS:-SUCCESS}"

echo "[1/5] Getting programs from ${BASE_URL}/api/programs"
PROGRAMS_RESPONSE=$(curl -sS -X GET "${BASE_URL}/api/programs")
echo "${PROGRAMS_RESPONSE}"

echo
echo "[2/5] Creating application"
APP_RESPONSE=$(curl -sS -X POST "${BASE_URL}/api/applications" \
  -H "Content-Type: application/json" \
  -d "{\"programId\":${PROGRAM_ID},\"tariffId\":${TARIFF_ID},\"userEmail\":\"${EMAIL}\",\"userName\":\"${NAME}\"}")
echo "${APP_RESPONSE}"

APP_ID=$(echo "${APP_RESPONSE}" | sed -n 's/.*"id"[[:space:]]*:[[:space:]]*\([0-9][0-9]*\).*/\1/p')
if [[ -z "${APP_ID}" ]]; then
  echo "ERROR: Cannot parse application id from response"
  exit 1
fi

echo
echo "[3/5] Sending bank webhook with status=${WEBHOOK_STATUS}"
curl -sS -X POST "${BASE_URL}/api/payments/webhook" \
  -H "Content-Type: application/json" \
  -d "{\"applicationId\":${APP_ID},\"status\":\"${WEBHOOK_STATUS}\"}"

echo
echo "[4/5] Reading application status"
STATUS_RESPONSE=$(curl -sS -X GET "${BASE_URL}/api/applications/${APP_ID}")
echo "${STATUS_RESPONSE}"

echo
echo "[5/5] Summary"
echo "Application ID: ${APP_ID}"
echo "Webhook status: ${WEBHOOK_STATUS}"
if echo "${STATUS_RESPONSE}" | grep -q '"status":"ENROLLED"'; then
  echo "Result: ENROLLED"
elif echo "${STATUS_RESPONSE}" | grep -q '"status":"ENROLLMENT_FAILED"'; then
  echo "Result: ENROLLMENT_FAILED"
elif echo "${STATUS_RESPONSE}" | grep -q '"status":"PAYMENT_FAILED"'; then
  echo "Result: PAYMENT_FAILED"
else
  echo "Result: see raw status above"
fi
