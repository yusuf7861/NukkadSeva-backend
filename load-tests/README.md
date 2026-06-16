# k6 Load Testing Guide

This folder contains a role-aware k6 test suite for NukkadSeva backend APIs.

## Files

- `main.js` - main k6 scenarios (public, customer, provider, admin)
- `k6.env.example` - environment variables template
- `endpoints.md` - endpoint-to-test coverage map

## Quick Start

1. Start backend (example):

```bash
cd /home/yjamal/HomeFix/NukkadSeva-backend
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

2. Prepare env:

```bash
cd /home/yjamal/HomeFix/NukkadSeva-backend/load-tests
cp k6.env.example .env
# edit .env and add BASE_URL + role credentials
```

3. Run read-heavy (safe) load:

```bash
cd /home/yjamal/HomeFix/NukkadSeva-backend
set -a
source load-tests/.env
set +a
k6 run load-tests/main.js
```

## Mutation Run (write endpoints)

Enable writes only when test data is isolated:

```bash
cd /home/yjamal/HomeFix/NukkadSeva-backend
set -a
source load-tests/.env
set +a
INCLUDE_MUTATIONS=true k6 run load-tests/main.js
```

For ID-based endpoints, set these in `.env`:

- `ADDRESS_ID`, `BOOKING_ID`, `SERVICE_ID`, `AREA_ID`, `CITY_ID`, `PROVIDER_ID`, `PENDING_PROVIDER_ID`

## Notes

- By default, checks enforce: no 5xx responses, latency thresholds, and overall check pass rate.
- If only one role credential is configured, only that role scenario is executed (public scenario always runs).
- `POST /api/customer/register` is intentionally not part of default load, to avoid creating large numbers of users.
- Multipart endpoints require file paths (`PROFILE_PIC_PATH`, `PROVIDER_DOC_PATH`).

## Optional: export HTML summary

```bash
cd /home/yjamal/HomeFix/NukkadSeva-backend
set -a
source load-tests/.env
set +a
K6_WEB_DASHBOARD=true K6_WEB_DASHBOARD_EXPORT=load-tests/result.html k6 run load-tests/main.js
```

