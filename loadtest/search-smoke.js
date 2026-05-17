import http from "k6/http";
import { check } from "k6";

export const options = {
  vus: 20,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<500", "p(99)<1000"]
  }
};

const BASE = __ENV.SEARCH_BASE_URL || "http://host.docker.internal:5020";

export default function () {
  const users = http.get(`${BASE}/api/search/users?q=ali&page=0&size=10`);
  check(users, { "users 200": (r) => r.status === 200 });

  const posts = http.get(`${BASE}/api/search/posts?q=hello&page=0&size=10`);
  check(posts, { "posts 200": (r) => r.status === 200 });

  const hashtags = http.get(`${BASE}/api/search/hashtags?q=dev&page=0&size=10`);
  check(hashtags, { "hashtags 200": (r) => r.status === 200 });
  
  const suggest = http.get(`${BASE}/api/search/suggest?q=al&limit=5`);
  check(suggest, {
    "suggest 200": (r) => r.status === 200,
    "debounce header": (r) => !!r.headers["X-Debounce-Friendly"]
  });
}; 
