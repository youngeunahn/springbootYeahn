import http from 'k6/http';
import { sleep } from 'k6';

const baseUrl = __ENV.BASE_URL;

if (!baseUrl) {
  throw new Error('BASE_URL is required. Example: BASE_URL=https://your-domain.com k6 run prod-load-test.js');
}

export const options = {
  stages: [
    { duration: '30s', target: 1 },
    { duration: '1m', target: 1 },
    { duration: '30s', target: 3 },
    { duration: '1m', target: 3 },
    { duration: '30s', target: 5 },
    { duration: '1m', target: 5 },
    { duration: '30s', target: 0 },
  ],
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<1000'],
  },
};

export default function () {
  http.get(`${baseUrl}/login`);
  sleep(1);
}
