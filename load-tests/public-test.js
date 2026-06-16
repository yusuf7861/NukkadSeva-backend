import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
    stages: [
        { duration: '20s', target: 10 },  // ramp up
        { duration: '40s', target: 10 },  // hold
        { duration: '20s', target: 0 },   // ramp down
    ],
};

const BASE_URL = 'http://localhost:8080';

export default function () {

    // 1. Get providers
    let res1 = http.get(`${BASE_URL}/api/public/providers?page=1&limit=10`);
    check(res1, {
        'providers status 200': (r) => r.status === 200,
        'providers < 500ms': (r) => r.timings.duration < 500,
    });

    // 2. Get cities
    let res2 = http.get(`${BASE_URL}/api/public/providers/cities`);
    check(res2, {
        'cities status 200': (r) => r.status === 200,
    });

    // 3. Search services
    let res3 = http.get(`${BASE_URL}/api/services/search?city=Mumbai`);
    check(res3, {
        'search status 200': (r) => r.status === 200,
    });

    sleep(1);
}