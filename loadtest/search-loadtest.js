import http from "k6/http";
import { check, sleep } from "k6";

function intFromEnv(name, fallback) {
  const raw = __ENV[name];
  if (!raw) {
    return fallback;
  }

  const parsed = Number.parseInt(raw, 10);
  return Number.isNaN(parsed) ? fallback : parsed;
}

function durationFromEnv(name, fallback) {
  return __ENV[name] || fallback;
}

const BASE = __ENV.SEARCH_BASE_URL || "http://localhost:5020";
const START_RATE = intFromEnv("K6_START_RATE", 20);
const PRE_ALLOCATED_VUS = intFromEnv("K6_PRE_ALLOCATED_VUS", 40);
const MAX_VUS = intFromEnv("K6_MAX_VUS", 400);

export const options = {
  scenarios: {
    mixed_search: {
      executor: "ramping-arrival-rate",
      startRate: START_RATE,
      timeUnit: "1s",
      preAllocatedVUs: PRE_ALLOCATED_VUS,
      maxVUs: MAX_VUS,
      stages: [
        { target: intFromEnv("K6_STAGE1_TARGET", 60), duration: durationFromEnv("K6_STAGE1_DURATION", "1m") },
        { target: intFromEnv("K6_STAGE2_TARGET", 120), duration: durationFromEnv("K6_STAGE2_DURATION", "2m") },
        { target: intFromEnv("K6_STAGE3_TARGET", 120), duration: durationFromEnv("K6_STAGE3_DURATION", "1m") },
        { target: 0, duration: durationFromEnv("K6_STAGE4_DURATION", "30s") }
      ]
    }
  },
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<400", "p(99)<800"]
  }
};

const terms = ["ali", "dilnoza", "java", "spring", "#nexora", "chat", "backend", "microservice"];

function pick(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

export default function () {
  const q = pick(terms);

  const usersRes = http.get(`${BASE}/api/search/users?q=${encodeURIComponent(q)}&page=0&size=20`);
  check(usersRes, {
    "users status 200": (r) => r.status === 200
  });

  const postsRes = http.get(`${BASE}/api/search/posts?q=${encodeURIComponent(q)}&page=0&size=20&minPopularity=0`);
  check(postsRes, {
    "posts status 200": (r) => r.status === 200
  });

  const hashtagsRes = http.get(`${BASE}/api/search/hashtags?q=${encodeURIComponent(q)}&page=0&size=20`);
  check(hashtagsRes, {
    "hashtags status 200": (r) => r.status === 200
  });

  const suggestRes = http.get(`${BASE}/api/search/suggest?q=${encodeURIComponent(q)}&limit=10`);
  check(suggestRes, {
    "suggest status 200": (r) => r.status === 200,
    "debounce header present": (r) => !!r.headers["X-Debounce-Friendly"]
  });

  sleep(0.1);
}
