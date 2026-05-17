# Nexora Search Service (Java, Spring Boot)

Scalable search microservice for users, posts, and hashtags.

## Stack
- Java 17+
- Spring Boot 3
- Elasticsearch (full-text + fuzzy + ranking)
- Redis (popular/frequent query caching)
- PostgreSQL (query analytics + trending hashtag aggregation)

## Endpoints
- `GET /api/search/users?q=&page=&size=`
- `GET /api/search/posts?q=&from=&to=&minPopularity=&page=&size=`
- `GET /api/search/hashtags?q=&page=&size=`
- `GET /api/search/suggest?q=&limit=`

## Run
```bash
mvn spring-boot:run
```

## Run with Docker (service + dependencies)
```bash
docker compose -f docker-compose.search.yml up -d --build
```

Service URL: `http://localhost:5020`

## Elasticsearch Index Setup

Indexes are auto-created on app startup by:
- `ElasticsearchIndexBootstrap`

Index definitions:
- `src/main/resources/elasticsearch/users-index.json`
- `src/main/resources/elasticsearch/posts-index.json`
- `src/main/resources/elasticsearch/hashtags-index.json`

Manual setup scripts:
- PowerShell: `scripts/elasticsearch/setup-indexes.ps1`
- Bash: `scripts/elasticsearch/setup-indexes.sh`
- PowerShell sample data seed: `scripts/elasticsearch/seed-sample-data.ps1`
- Bash sample data seed: `scripts/elasticsearch/seed-sample-data.sh`

Examples:
```powershell
./scripts/elasticsearch/setup-indexes.ps1 -EsUrl http://localhost:9200
```

```bash
./scripts/elasticsearch/setup-indexes.sh http://localhost:9200
```

Seed sample data:

```powershell
./scripts/elasticsearch/seed-sample-data.ps1 -EsUrl http://localhost:9200
```

```bash
./scripts/elasticsearch/seed-sample-data.sh http://localhost:9200
```

## Architecture
Frontend -> Spring Boot Search API -> Elasticsearch -> PostgreSQL
                                       |
                                       -> Redis cache

## Notes
- Fast-path empty/short query responses are included for debounce-friendly UX.
- Query logs are written asynchronously for analytics.
- Trending hashtag fallback is provided from last 24h usage data in PostgreSQL.

## k6 Load Test (Performance)

Load test script:
- `loadtest/search-loadtest.js`
- `loadtest/search-smoke.js`

Run:
```bash
k6 run -e SEARCH_BASE_URL=http://localhost:5020 loadtest/search-loadtest.js
```

Local-friendly defaults are enabled in `search-loadtest.js`. You can override with env vars:
- `K6_START_RATE`, `K6_PRE_ALLOCATED_VUS`, `K6_MAX_VUS`
- `K6_STAGE1_TARGET`, `K6_STAGE2_TARGET`, `K6_STAGE3_TARGET`
- `K6_STAGE1_DURATION`, `K6_STAGE2_DURATION`, `K6_STAGE3_DURATION`, `K6_STAGE4_DURATION`

Smoke test:

```bash
k6 run -e SEARCH_BASE_URL=http://localhost:5020 loadtest/search-smoke.js
```

CI smoke workflow:
- `.github/workflows/search-smoke.yml`

Current thresholds in script:
- `p95 < 200ms`
- `p99 < 350ms`
- error rate `< 1%`
