#!/usr/bin/env bash
set -euo pipefail

ES_URL="${1:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_DIR="$SCRIPT_DIR/../../loadtest/sample-data"

for index in users_search posts_search hashtags_search; do
  if ! curl -fsS -o /dev/null "$ES_URL/$index"; then
    echo "Index not found: $index"
    exit 1
  fi
done

bulk_load() {
  local index="$1"
  local file="$2"
  curl -fsS -X POST "$ES_URL/$index/_bulk?refresh=true" \
    -H "Content-Type: application/x-ndjson" \
    --data-binary "@$DATA_DIR/$file" >/dev/null
  echo "Seeded: $index"
}

bulk_load "users_search" "users.ndjson"
bulk_load "posts_search" "posts.ndjson"
bulk_load "hashtags_search" "hashtags.ndjson"

echo "Sample data seeding completed."
