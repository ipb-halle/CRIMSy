#!/bin/bash
# Generates TypeScript API client from OpenAPI spec

# Stop on error
set -e

# Paths
SPEC_FILE="../ui/src/main/resources/usert-api.yml"
OUTPUT_DIR="src/adapters/api"

# OpenAPI Generator command
openapi-generator-cli generate \
  -i "$SPEC_FILE" \
  -g typescript-fetch \
  -o "$OUTPUT_DIR" \
  --additional-properties=supportsES6=true,modelPackage=models,apiPackage=apis

echo "✅ API client regenerated in $OUTPUT_DIR"
