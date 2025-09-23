#!/bin/bash
JENKINS_USER="${JENKINS_USER_ID}"
JENKINS_TOKEN="${JENKINS_API_TOKEN}" # replace with your token or set env var

INPUT_FILE="$1"
OUTPUT_FILE="$2"

if [ -z "$INPUT_FILE" ] || [ -z "$OUTPUT_FILE" ]; then
  echo "Error: Both an input file and an output file path are required." >&2
  echo "Usage: $0 <path_to_input_file> <path_to_output_file>" >&2
  exit 1
fi

if [ ! -f "$INPUT_FILE" ]; then
  echo "Error: File not found at '$INPUT_FILE'." >&2
  exit 1
fi

# Initialize/clear the output file at the start of the run
> "$OUTPUT_FILE"

echo "Processing URLs from '$INPUT_FILE'..." >&2

while IFS= read -r JOB_URL || [[ -n "$JOB_URL" ]]; do
  if [ -z "$JOB_URL" ]; then
    continue
  fi

  api_response=$(curl -s -u "${JENKINS_USER}:${JENKINS_TOKEN}" "${JOB_URL}/api/json" -w "\n%{http_code}")
  http_code=$(echo "$api_response" | tail -n1)
  json_body=$(echo "$api_response" | sed '$d')

  # On error, print a warning to stderr and continue to the next URL
  if [ "$http_code" != "200" ]; then
    echo "Warning: Could not fetch '${JOB_URL}'. HTTP status: ${http_code}. Skipping." >&2
    continue
  fi

  # Parse the JSON and check the 'buildable' status
  is_buildable=$(echo "$json_body" | jq '.buildable')

  # If the job is buildable (enabled), print its URL to standard output
  if [ "$is_buildable" == "true" ]; then
    echo "$JOB_URL" >> "$OUTPUT_FILE"
  fi

done < "$INPUT_FILE"

echo "Processing complete. Enabled job URLs saved to '$OUTPUT_FILE'" >&2