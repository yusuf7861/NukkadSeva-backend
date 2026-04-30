# Endpoint Coverage Matrix (k6)

This matrix maps API endpoints to `load-tests/main.js` coverage.

## Public and Auth

- `POST /api/login` - covered in `setup()` (role logins)
- `POST /api/logout` - covered in `teardown()`
- `GET /api/public/providers` - covered
- `GET /api/public/providers/cities` - covered
- `GET /api/services/search` - covered
- `GET /api/verify-email` - optional (`VERIFY_EMAIL_TOKEN`)
- `GET /api/provider/verify-email` - optional (`PROVIDER_VERIFY_EMAIL_TOKEN`)
- `POST /api/forgot-password` - optional (`FORGOT_PASSWORD_EMAIL`)
- `POST /api/reset-password` - optional (`FORGOT_PASSWORD_EMAIL`, `RESET_OTP`, `RESET_NEW_PASSWORD`)
- `POST /api/auth/google` - optional (`GOOGLE_ID_TOKEN`)

## Customer

- `POST /api/customer/register` - not run by default; avoid creating many users during load
- `GET /api/customer/profile` - covered
- `PUT /api/customer/profile` - covered when `INCLUDE_MUTATIONS=true`
- `GET /api/customer/dashboard` - covered
- `GET /api/customer/address` - covered
- `POST /api/customer/address` - covered when `INCLUDE_MUTATIONS=true`
- `PUT /api/customer/address/{id}` - covered when `INCLUDE_MUTATIONS=true` and `ADDRESS_ID` set
- `DELETE /api/customer/address/{id}` - covered when `INCLUDE_MUTATIONS=true` and `ADDRESS_ID` set
- `PUT /api/customer/address/{id}/default` - covered when `INCLUDE_MUTATIONS=true` and `ADDRESS_ID` set
- `PUT /api/update-profile-picture` - covered when `INCLUDE_MUTATIONS=true` and `PROFILE_PIC_PATH` set

## Provider

- `POST /api/provider/register` - optional when `INCLUDE_MUTATIONS=true`, `ENABLE_PROVIDER_REGISTRATION=true`, `PROVIDER_DOC_PATH` set
- `GET /api/provider/profile` - covered
- `GET /api/provider/dashboard` - covered
- `POST /api/provider/areas` - covered when `INCLUDE_MUTATIONS=true`
- `GET /api/provider/areas` - covered
- `DELETE /api/provider/areas/{areaId}` - covered when `INCLUDE_MUTATIONS=true` and `AREA_ID` set

## Services

- `POST /api/services` - covered when `INCLUDE_MUTATIONS=true`
- `GET /api/services/me` - covered
- `PATCH /api/services/{id}/toggle-status` - covered when `INCLUDE_MUTATIONS=true` and `SERVICE_ID` set

## Booking

- `POST /api/booking` - covered when `INCLUDE_MUTATIONS=true` and `PROVIDER_ID`, `ADDRESS_ID` set
- `GET /api/booking/customer` - covered
- `GET /api/booking/provider` - covered
- `PUT /api/booking/{id}/respond` - covered when `INCLUDE_MUTATIONS=true` and `BOOKING_ID` set
- `PUT /api/booking/{id}/cancel` - covered when `INCLUDE_MUTATIONS=true` and `BOOKING_ID` set
- `PUT /api/booking/{id}/complete` - covered when `INCLUDE_MUTATIONS=true` and `BOOKING_ID`, `BOOKING_COMPLETION_OTP` set

## Reviews

- `POST /api/reviews` - covered when `INCLUDE_MUTATIONS=true` and `BOOKING_ID` set
- `GET /api/reviews/provider` - covered

## Admin

- `GET /api/admin/providers/pending` - covered
- `GET /api/admin/providers/approved` - covered
- `GET /api/admin/providers/rejected` - covered
- `GET /api/admin/all-providers` - covered
- `GET /api/admin/providers/{id}` - covered when `INCLUDE_MUTATIONS=true` and `PENDING_PROVIDER_ID` or `PROVIDER_ID` set
- `POST /api/admin/providers/{id}/approve` - covered when `INCLUDE_MUTATIONS=true` and `PENDING_PROVIDER_ID` or `PROVIDER_ID` set
- `POST /api/admin/providers/{id}/reject` - covered when `INCLUDE_MUTATIONS=true` and `PENDING_PROVIDER_ID` or `PROVIDER_ID` set
- `GET /api/admin/cities` - covered
- `POST /api/admin/cities` - covered when `INCLUDE_MUTATIONS=true`
- `POST /api/admin/cities/{cityId}/pincodes` - covered when `INCLUDE_MUTATIONS=true` and `CITY_ID` set
- `PATCH /api/admin/cities/{cityId}/status` - covered when `INCLUDE_MUTATIONS=true` and `CITY_ID` set
- `DELETE /api/admin/cities/{cityId}` - covered when `INCLUDE_MUTATIONS=true` and `CITY_ID` set

