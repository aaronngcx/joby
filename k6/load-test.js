import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '30s', target: 10 },  // ramp up to 10 users
        { duration: '1m', target: 10 },   // stay at 10 users
        { duration: '30s', target: 0 },   // ramp down
    ],
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% of requests under 500ms
        http_req_failed: ['rate<0.01'],    // less than 1% failure rate
    },
};

const BASE_URL = 'http://localhost:8080';
const API_KEY = 'joby-secret-key-123';

export default function () {
    // enqueue a job
    const payload = JSON.stringify({
        type: 'EMAIL_NOTIFICATION',
        payload: 'load test',
        priority: Math.floor(Math.random() * 10),
    });

    const enqueueRes = http.post(`${BASE_URL}/api/jobs`, payload, {
        headers: {
            'Content-Type': 'application/json',
            'X-API-KEY': API_KEY,
        },
    });

    check(enqueueRes, {
        'enqueue status 200': (r) => r.status === 200,
        'enqueue has id': (r) => JSON.parse(r.body).id !== undefined,
    });

    sleep(1);

    // check stats
    const statsRes = http.get(`${BASE_URL}/api/jobs/stats`, {
        headers: { 'X-API-KEY': API_KEY },
    });

    check(statsRes, {
        'stats status 200': (r) => r.status === 200,
    });

    sleep(1);
}