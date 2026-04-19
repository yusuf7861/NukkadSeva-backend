# NukkadSeva Backend — Feature Expansion Plan

## Purpose
This document translates the proposed product improvements into an implementation-ready plan for the backend team. It focuses on API design, data model changes, rollout phases, and engineering guardrails.

## Current Baseline (What already exists)
- JWT authentication with role-based authorization (`CUSTOMER`, `SERVICE_PROVIDER`, `ADMIN`).
- Booking lifecycle (create, provider respond, customer cancel, provider complete with OTP).
- Real-time booking updates via WebSocket user queues.
- Admin workflows for provider approval/rejection and city/pincode management.
- Basic rate limiting via Bucket4j.

---

## Feature 1: Payments & Invoices

### Goals
- Collect online payment safely for bookings.
- Support payment confirmation webhooks.
- Enable refund and invoice workflows.

### Backend Scope
1. **Payment entities / tables**
   - `payment_transaction`
   - `payment_refund`
   - `invoice`
2. **Payment APIs**
   - `POST /api/payments/create-intent`
   - `POST /api/payments/confirm`
   - `POST /api/payments/webhook` (provider callback)
   - `POST /api/payments/{transactionId}/refund` (admin/support)
   - `GET /api/payments/{bookingId}`
3. **Booking integration**
   - Introduce stronger state transitions:
     - `PENDING_PAYMENT -> PAID -> APPROVED -> COMPLETED`
   - Prevent provider acceptance for unpaid bookings (configurable).
4. **Invoice generation**
   - PDF invoice metadata and download URL endpoint.

### Security & Reliability
- Verify webhook signatures.
- Idempotency key support for payment intent creation.
- Persist provider gateway response IDs for reconciliation.

### Acceptance Criteria
- Duplicate payment attempts do not double-charge.
- Webhook replay does not change final state incorrectly.
- Refund updates booking/payment status consistently.

---

## Feature 2: Availability Calendar & Slot Engine

### Goals
- Let providers declare weekly availability.
- Prevent double-booking.
- Enable customer slot discovery.

### Backend Scope
1. **Data model**
   - `provider_availability` (weekday, start, end)
   - `provider_blocked_slot` (date-time ranges, reason)
   - optional materialized `provider_slot`
2. **APIs**
   - `PUT /api/provider/availability`
   - `GET /api/public/providers/{id}/slots?date=YYYY-MM-DD`
   - `POST /api/provider/blocked-slots`
   - `DELETE /api/provider/blocked-slots/{id}`
3. **Booking validation**
   - Booking creation checks:
     - slot exists
     - slot not blocked
     - no overlap with active booking

### Acceptance Criteria
- Overlapping bookings are rejected with clear validation message.
- Timezone handling is deterministic (store UTC, render local).

---

## Feature 3: Search Ranking & Discovery

### Goals
- Improve provider discoverability and conversion.
- Return ranked results by relevance.

### Backend Scope
1. **Query filters**
   - rating range, price range, distance radius, availability-now, verified-only.
2. **Sorting**
   - `best_match`, `top_rated`, `nearest`, `price_low_to_high`.
3. **Ranking score**
   - weighted score from: rating, response speed, completion rate, profile completeness, location proximity.
4. **API extension**
   - Upgrade `GET /api/public/providers` with new query params and ranking metadata.

### Acceptance Criteria
- Search API includes normalized `score` for each result.
- Pagination remains stable under deterministic sorting.

---

## Feature 4: In-App Chat (Booking-linked)

### Goals
- Allow customer-provider communication tied to a booking.
- Preserve conversation history for support/dispute.

### Backend Scope
1. **Data model**
   - `chat_thread` (booking-linked)
   - `chat_message` (senderId, body, sentAt, readAt)
2. **APIs / WebSocket**
   - REST:
     - `GET /api/chat/threads`
     - `GET /api/chat/threads/{id}/messages`
   - WebSocket:
     - publish `/app/chat.send`
     - subscribe `/user/queue/chat`
3. **Moderation hooks**
   - profanity flagging / report message endpoint.

### Acceptance Criteria
- Messages are persisted and delivered in order.
- Read receipts update correctly for the counterparty.

---

## Feature 5: Review Moderation & Trust

### Goals
- Reduce spam/abuse in reviews.
- Build trust in provider ratings.

### Backend Scope
1. **Rules**
   - Only completed bookings can be reviewed.
   - One review per booking (update allowed with constraints).
2. **Moderation**
   - `POST /api/reviews/{id}/report`
   - `GET /api/admin/reviews/reported`
   - `PATCH /api/admin/reviews/{id}` (hide/unhide/action)
3. **Auditability**
   - store moderation reason and moderator identity.

### Acceptance Criteria
- Reported reviews become visible to admin queue immediately.
- Hidden reviews excluded from public provider aggregates.

---

## Feature 6: Provider Growth & Analytics

### Goals
- Help providers improve profile quality and conversion.

### Backend Scope
1. **Metrics endpoints**
   - profile views
   - booking conversion funnel
   - cancellation rates
   - avg response time
2. **Profile completeness engine**
   - weighted checklist and missing-fields hints.
3. **Optional recommendations**
   - city/service price band suggestions.

### Acceptance Criteria
- Dashboard responses include trend windows (`7d`, `30d`, `90d`).
- Metrics query performance acceptable under pagination/windowing.

---

## Feature 7: Reliability & Scale Enhancements

### Goals
- Make booking and payment flows resilient under retries and load.

### Backend Scope
1. **Idempotency layer**
   - support `Idempotency-Key` for sensitive POST APIs.
2. **Distributed throttling/cache**
   - move from in-memory bucket storage to Redis-based strategy.
3. **Async processing**
   - event queue/outbox for webhook and notification retries.
4. **Observability**
   - structured logging with correlation IDs.
   - endpoint-level latency/error metrics.

### Acceptance Criteria
- Duplicate requests return same semantic result without side effects.
- Retry workers guarantee at-least-once delivery with dedupe safeguards.

---

## Feature 8: Notification Expansion

### Goals
- Multi-channel communication with user preference control.

### Backend Scope
1. **Channels**
   - email, websocket, push notification (FCM/APNS).
2. **User preferences**
   - per-event opt-in/out
   - quiet hours
3. **Delivery state**
   - `notification_event` and `notification_delivery` records.

### Acceptance Criteria
- Users can disable non-essential events.
- Delivery status is queryable for support/debugging.

---

## Suggested Delivery Plan

## Phase 1 (Weeks 1–6)
- Payments & webhooks
- Slot overlap prevention
- Idempotency for booking/payment create

## Phase 2 (Weeks 7–12)
- Search ranking
- In-app chat
- Notification preferences

## Phase 3 (Weeks 13+)
- Analytics and growth tooling
- Moderation and trust refinements
- Redis-backed rate limiting + observability hardening

---

## Cross-Cutting Engineering Standards
- Add migration files for every schema change (Liquibase changelog updates).
- Add unit + integration tests for each new endpoint and state transition.
- Add API docs/OpenAPI examples for new request/response models.
- Add role-authorization tests for admin/provider/customer boundaries.
- Enforce backward compatibility for existing frontend flows where possible.

## Definition of Done (for each feature)
- API contract documented.
- DB migration reviewed.
- Security review complete.
- Automated tests passing in CI.
- Monitoring/alerts added for critical paths.
