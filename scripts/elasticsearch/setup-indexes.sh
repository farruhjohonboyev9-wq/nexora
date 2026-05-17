#!/usr/bin/env bash
set -euo pipefail

ES_URL="${1:-http://localhost:9200}"
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
RESOURCE_DIR="$SCRIPT_DIR/../../src/main/resources/elasticsearch"

create_index() {
  local name="$1"
  local file="$2"

  if curl -fsS -o /dev/null "$ES_URL/$name"; then
    echo "Index exists: $name"
    return 0
  fi

  curl -fsS -X PUT "$ES_URL/$name" \
    -H "Content-Type: application/json" \
    --data-binary "@$RESOURCE_DIR/$file" >/dev/null

  echo "Created index: $name"
}

create_index "users_search" "users-index.json"
create_index "posts_search" "posts-index.json"
create_index "hashtags_search" "hashtags-index.json"
