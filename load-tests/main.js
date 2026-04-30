import http from 'k6/http';
import { check, group, sleep } from 'k6';

const BASE_URL = (__ENV.BASE_URL || 'http://localhost:8080').replace(/\/$/, '');
const INCLUDE_MUTATIONS = (__ENV.INCLUDE_MUTATIONS || 'false') === 'true';
const PAUSE_SECONDS = Number(__ENV.PAUSE_SECONDS || 0.25);

const hasCustomerCreds = Boolean(__ENV.CUSTOMER_EMAIL && __ENV.CUSTOMER_PASSWORD);
const hasProviderCreds = Boolean(__ENV.PROVIDER_EMAIL && __ENV.PROVIDER_PASSWORD);
const hasAdminCreds = Boolean(__ENV.ADMIN_EMAIL && __ENV.ADMIN_PASSWORD);

const profilePic = __ENV.PROFILE_PIC_PATH ? open(__ENV.PROFILE_PIC_PATH, 'b') : null;
const providerDoc = __ENV.PROVIDER_DOC_PATH ? open(__ENV.PROVIDER_DOC_PATH, 'b') : null;

const defaultHeaders = {
  'Content-Type': 'application/json',
  Accept: 'application/json',
};

const scenarios = {
  public_read: {
    executor: 'ramping-vus',
    exec: 'publicReadFlow',
    startVUs: 1,
    stages: [
      { duration: __ENV.PUBLIC_RAMP_UP || '20s', target: Number(__ENV.PUBLIC_VUS || 5) },
      { duration: __ENV.PUBLIC_HOLD || '40s', target: Number(__ENV.PUBLIC_VUS || 5) },
      { duration: __ENV.PUBLIC_RAMP_DOWN || '20s', target: 0 },
    ],
  },
};

if (hasCustomerCreds) {
  scenarios.customer_flow = {
    executor: 'constant-vus',
    exec: 'customerFlow',
    vus: Number(__ENV.CUSTOMER_VUS || 2),
    duration: __ENV.CUSTOMER_DURATION || '1m',
  };
}

if (hasProviderCreds) {
  scenarios.provider_flow = {
    executor: 'constant-vus',
    exec: 'providerFlow',
    vus: Number(__ENV.PROVIDER_VUS || 2),
    duration: __ENV.PROVIDER_DURATION || '1m',
  };
}

if (hasAdminCreds) {
  scenarios.admin_flow = {
    executor: 'constant-vus',
    exec: 'adminFlow',
    vus: Number(__ENV.ADMIN_VUS || 1),
    duration: __ENV.ADMIN_DURATION || '1m',
  };
}

export const options = {
  scenarios,
  thresholds: {
    http_req_failed: ['rate<0.15'],
    http_req_duration: ['p(95)<2500', 'p(99)<4500'],
    checks: ['rate>0.90'],
  },
};

function parseJson(response) {
  try {
    return response.json();
  } catch (e) {
    return null;
  }
}

function tokenFromAuthResponse(response) {
  const body = parseJson(response);
  return body && body.access_token ? body.access_token : null;
}

function authHeaders(token) {
  return token
    ? {
        ...defaultHeaders,
        Authorization: `Bearer ${token}`,
      }
    : defaultHeaders;
}

function apiRequest(method, path, params = {}) {
  const url = `${BASE_URL}${path}`;
  const payload = params.payload === undefined ? null : params.payload;
  const headers = params.headers || defaultHeaders;
  const tags = params.tags || {};

  let response;
  const contentType = headers['Content-Type'] || headers['content-type'] || '';

  if (payload === null) {
    response = http.request(method, url, null, { headers, tags });
  } else if (typeof payload === 'string') {
    response = http.request(method, url, payload, { headers, tags });
  } else if (contentType.includes('application/json')) {
    response = http.request(method, url, JSON.stringify(payload), { headers, tags });
  } else {
    response = http.request(method, url, payload, { headers, tags });
  }

  check(response, {
    [`${method} ${path} does not return 5xx`]: (r) => r.status < 500,
  });

  return response;
}

function login(email, password) {
  const response = apiRequest('POST', '/api/login', {
    payload: { email, password },
    tags: { endpoint: '/api/login' },
  });

  check(response, {
    'login succeeded': (r) => r.status === 200,
  });

  return tokenFromAuthResponse(response);
}

function maybe(pathEnvName) {
  const value = __ENV[pathEnvName];
  return value && value.trim() !== '' ? value.trim() : null;
}

function firstValue(...values) {
  for (const value of values) {
    if (value !== null && value !== undefined && `${value}`.trim() !== '') {
      return value;
    }
  }
  return null;
}

function maybePublicProviderId() {
  const response = apiRequest('GET', '/api/public/providers?page=1&limit=1', {
    tags: { endpoint: '/api/public/providers' },
  });

  const body = parseJson(response);
  if (!body || !Array.isArray(body.providers) || body.providers.length === 0) {
    return null;
  }

  const provider = body.providers[0];
  return firstValue(provider.id, provider.providerId, provider.profileId);
}

export function setup() {
  const state = {
    customerToken: null,
    providerToken: null,
    adminToken: null,
    providerId: firstValue(maybe('PROVIDER_ID'), maybePublicProviderId()),
    serviceId: maybe('SERVICE_ID'),
    bookingId: maybe('BOOKING_ID'),
    addressId: maybe('ADDRESS_ID'),
    areaId: maybe('AREA_ID'),
    cityId: maybe('CITY_ID'),
    pendingProviderId: maybe('PENDING_PROVIDER_ID'),
  };

  if (hasCustomerCreds) {
    state.customerToken = login(__ENV.CUSTOMER_EMAIL, __ENV.CUSTOMER_PASSWORD);
  }
  if (hasProviderCreds) {
    state.providerToken = login(__ENV.PROVIDER_EMAIL, __ENV.PROVIDER_PASSWORD);
  }
  if (hasAdminCreds) {
    state.adminToken = login(__ENV.ADMIN_EMAIL, __ENV.ADMIN_PASSWORD);
  }

  return state;
}

export function publicReadFlow(data) {
  group('public endpoints', () => {
    apiRequest('GET', '/api/public/providers?page=1&limit=6', {
      tags: { endpoint: '/api/public/providers' },
    });
    apiRequest('GET', '/api/public/providers/cities', {
      tags: { endpoint: '/api/public/providers/cities' },
    });

    const searchByCity = maybe('SEARCH_CITY') || 'Mumbai';
    apiRequest('GET', `/api/services/search?city=${encodeURIComponent(searchByCity)}`, {
      tags: { endpoint: '/api/services/search' },
    });

    if (data.providerId) {
      apiRequest('GET', `/api/services/search?providerId=${data.providerId}`, {
        tags: { endpoint: '/api/services/search' },
      });
    }

    if (maybe('VERIFY_EMAIL_TOKEN')) {
      apiRequest('GET', `/api/verify-email?token=${encodeURIComponent(maybe('VERIFY_EMAIL_TOKEN'))}`, {
        tags: { endpoint: '/api/verify-email' },
      });
    }

    if (maybe('PROVIDER_VERIFY_EMAIL_TOKEN')) {
      apiRequest(
        'GET',
        `/api/provider/verify-email?token=${encodeURIComponent(maybe('PROVIDER_VERIFY_EMAIL_TOKEN'))}`,
        { tags: { endpoint: '/api/provider/verify-email' } }
      );
    }

    if (maybe('FORGOT_PASSWORD_EMAIL')) {
      apiRequest('POST', '/api/forgot-password', {
        payload: { email: maybe('FORGOT_PASSWORD_EMAIL') },
        tags: { endpoint: '/api/forgot-password' },
      });
    }

    if (maybe('RESET_OTP') && maybe('RESET_NEW_PASSWORD') && maybe('FORGOT_PASSWORD_EMAIL')) {
      apiRequest('POST', '/api/reset-password', {
        payload: {
          email: maybe('FORGOT_PASSWORD_EMAIL'),
          otp: maybe('RESET_OTP'),
          newPassword: maybe('RESET_NEW_PASSWORD'),
        },
        tags: { endpoint: '/api/reset-password' },
      });
    }

    if (maybe('GOOGLE_ID_TOKEN')) {
      apiRequest('POST', '/api/auth/google', {
        payload: { idToken: maybe('GOOGLE_ID_TOKEN') },
        tags: { endpoint: '/api/auth/google' },
      });
    }
  });

  sleep(PAUSE_SECONDS);
}

export function customerFlow(data) {
  if (!data.customerToken) {
    return;
  }

  const headers = authHeaders(data.customerToken);

  group('customer read endpoints', () => {
    apiRequest('GET', '/api/customer/profile', { headers, tags: { endpoint: '/api/customer/profile' } });
    apiRequest('GET', '/api/customer/dashboard', { headers, tags: { endpoint: '/api/customer/dashboard' } });
    apiRequest('GET', '/api/customer/address', { headers, tags: { endpoint: '/api/customer/address' } });
    apiRequest('GET', '/api/booking/customer', { headers, tags: { endpoint: '/api/booking/customer' } });
    apiRequest('GET', '/api/reviews/provider', { headers, tags: { endpoint: '/api/reviews/provider' } });
  });

  if (INCLUDE_MUTATIONS) {
    group('customer mutation endpoints', () => {
      apiRequest('PUT', '/api/customer/profile', {
        headers,
        payload: {
          name: maybe('CUSTOMER_NAME') || 'Load Test User',
          phone: maybe('CUSTOMER_PHONE') || '9000000000',
          city: maybe('SEARCH_CITY') || 'Mumbai',
          state: maybe('SEARCH_STATE') || 'Maharashtra',
          pincode: maybe('SEARCH_PINCODE') || '400001',
          fullAddress: maybe('CUSTOMER_FULL_ADDRESS') || 'Test Lane, Mumbai',
        },
        tags: { endpoint: '/api/customer/profile' },
      });

      apiRequest('POST', '/api/customer/address', {
        headers,
        payload: {
          type: 'HOME',
          area: maybe('SEARCH_AREA') || 'Andheri East',
          city: maybe('SEARCH_CITY') || 'Mumbai',
          state: maybe('SEARCH_STATE') || 'Maharashtra',
          pincode: maybe('SEARCH_PINCODE') || '400001',
          landmark: 'Near station',
          isDefault: true,
        },
        tags: { endpoint: '/api/customer/address' },
      });

      if (data.addressId) {
        apiRequest('PUT', `/api/customer/address/${data.addressId}`, {
          headers,
          payload: {
            type: 'HOME',
            area: maybe('SEARCH_AREA') || 'Andheri East',
            city: maybe('SEARCH_CITY') || 'Mumbai',
            state: maybe('SEARCH_STATE') || 'Maharashtra',
            pincode: maybe('SEARCH_PINCODE') || '400001',
            landmark: 'Updated by k6',
            isDefault: true,
          },
          tags: { endpoint: '/api/customer/address/{id}' },
        });

        apiRequest('PUT', `/api/customer/address/${data.addressId}/default`, {
          headers,
          tags: { endpoint: '/api/customer/address/{id}/default' },
        });

        apiRequest('DELETE', `/api/customer/address/${data.addressId}`, {
          headers,
          tags: { endpoint: '/api/customer/address/{id}' },
        });
      }

      if (data.providerId && data.addressId) {
        apiRequest('POST', '/api/booking', {
          headers,
          payload: {
            providerId: Number(data.providerId),
            serviceType: maybe('SERVICE_TYPE') || 'PLUMBING',
            bookingDateTime: maybe('BOOKING_DATETIME') || '2030-01-01T11:00:00',
            priceEstimate: Number(maybe('PRICE_ESTIMATE') || 500),
            finalPrice: Number(maybe('FINAL_PRICE') || 500),
            paymentMethod: maybe('PAYMENT_METHOD') || 'UPI',
            addressId: Number(data.addressId),
            note: 'k6 booking test',
          },
          tags: { endpoint: '/api/booking' },
        });
      }

      if (data.bookingId) {
        apiRequest('PUT', `/api/booking/${data.bookingId}/cancel`, {
          headers,
          tags: { endpoint: '/api/booking/{id}/cancel' },
        });

        apiRequest('POST', '/api/reviews', {
          headers,
          payload: {
            bookingId: data.bookingId,
            rating: Number(maybe('REVIEW_RATING') || 5),
            comment: 'Reviewed from k6 load test',
          },
          tags: { endpoint: '/api/reviews' },
        });
      }

      if (profilePic) {
        const form = {
          file: http.file(profilePic, 'profile-picture.jpg', 'image/jpeg'),
        };
        apiRequest('PUT', '/api/update-profile-picture', {
          headers: {
            Authorization: `Bearer ${data.customerToken}`,
            Accept: 'application/json',
          },
          payload: form,
          tags: { endpoint: '/api/update-profile-picture' },
        });
      }
    });
  }

  sleep(PAUSE_SECONDS);
}

export function providerFlow(data) {
  if (!data.providerToken) {
    return;
  }

  const headers = authHeaders(data.providerToken);

  group('provider read endpoints', () => {
    apiRequest('GET', '/api/provider/profile', { headers, tags: { endpoint: '/api/provider/profile' } });
    apiRequest('GET', '/api/provider/dashboard', { headers, tags: { endpoint: '/api/provider/dashboard' } });
    apiRequest('GET', '/api/provider/areas', { headers, tags: { endpoint: '/api/provider/areas' } });
    apiRequest('GET', '/api/services/me', { headers, tags: { endpoint: '/api/services/me' } });
    apiRequest('GET', '/api/booking/provider', { headers, tags: { endpoint: '/api/booking/provider' } });
  });

  if (INCLUDE_MUTATIONS) {
    group('provider mutation endpoints', () => {
      apiRequest('POST', '/api/provider/areas', {
        headers,
        payload: {
          city: maybe('SEARCH_CITY') || 'Mumbai',
          pincodes: [maybe('SEARCH_PINCODE') || '400001'],
        },
        tags: { endpoint: '/api/provider/areas' },
      });

      apiRequest('POST', '/api/services', {
        headers,
        payload: {
          name: maybe('SERVICE_NAME') || 'Pipe Repair',
          description: 'Created from k6 test',
          category: maybe('SERVICE_CATEGORY') || 'PLUMBING',
          price: Number(maybe('SERVICE_PRICE') || 300),
          durationMinutes: Number(maybe('SERVICE_DURATION_MINUTES') || 45),
          isActive: true,
          pincodes: [maybe('SEARCH_PINCODE') || '400001'],
        },
        tags: { endpoint: '/api/services' },
      });

      if (data.areaId) {
        apiRequest('DELETE', `/api/provider/areas/${data.areaId}`, {
          headers,
          tags: { endpoint: '/api/provider/areas/{id}' },
        });
      }

      if (data.serviceId) {
        apiRequest('PATCH', `/api/services/${data.serviceId}/toggle-status`, {
          headers,
          tags: { endpoint: '/api/services/{id}/toggle-status' },
        });
      }

      if (data.bookingId) {
        apiRequest('PUT', `/api/booking/${data.bookingId}/respond?action=ACCEPT`, {
          headers,
          tags: { endpoint: '/api/booking/{id}/respond' },
        });

        if (maybe('BOOKING_COMPLETION_OTP')) {
          apiRequest('PUT', `/api/booking/${data.bookingId}/complete?otp=${encodeURIComponent(maybe('BOOKING_COMPLETION_OTP'))}`, {
            headers,
            tags: { endpoint: '/api/booking/{id}/complete' },
          });
        }
      }
    });
  }

  sleep(PAUSE_SECONDS);
}

function addProviderRegistrationHit() {
  if (!providerDoc) {
    return;
  }

  const unique = `${Date.now()}-${Math.floor(Math.random() * 100000)}`;
  const payload = {
    fullName: maybe('PROVIDER_FULL_NAME') || 'k6 Provider',
    dob: maybe('PROVIDER_DOB') || '1990-01-01',
    mobileNumber: maybe('PROVIDER_MOBILE') || `9${Math.floor(100000000 + Math.random() * 899999999)}`,
    email: maybe('PROVIDER_REG_EMAIL') || `k6-provider-${unique}@example.com`,
    businessName: maybe('PROVIDER_BUSINESS') || 'k6 Services',
    serviceCategory: maybe('PROVIDER_SERVICE_CATEGORY') || 'PLUMBING',
    serviceArea: maybe('PROVIDER_SERVICE_AREA') || 'Mumbai',
    experience: Number(maybe('PROVIDER_EXPERIENCE') || 3),
    languages: maybe('PROVIDER_LANGUAGES') || 'English,Hindi',
    fullAddress: maybe('PROVIDER_ADDRESS') || 'k6 test address',
    state: maybe('SEARCH_STATE') || 'Maharashtra',
    city: maybe('SEARCH_CITY') || 'Mumbai',
    pincode: maybe('SEARCH_PINCODE') || '400001',
    gstin: maybe('PROVIDER_GSTIN') || '22AAAAA0000A1Z5',
    bio: maybe('PROVIDER_BIO') || 'Generated by k6 test',
    availability: maybe('PROVIDER_AVAILABILITY') || 'Mon-Sat',
    agreeToS: true,
    agreeToBgCheck: true,
    photograph: http.file(providerDoc, 'photograph.jpg', 'image/jpeg'),
    govtId: http.file(providerDoc, 'govtId.jpg', 'image/jpeg'),
    qualification: http.file(providerDoc, 'qualification.jpg', 'image/jpeg'),
    policeVerification: http.file(providerDoc, 'policeVerification.jpg', 'image/jpeg'),
    profilePicture: http.file(providerDoc, 'profilePicture.jpg', 'image/jpeg'),
  };

  apiRequest('POST', '/api/provider/register', {
    headers: { Accept: 'application/json' },
    payload,
    tags: { endpoint: '/api/provider/register' },
  });
}

export function adminFlow(data) {
  if (!data.adminToken) {
    return;
  }

  const headers = authHeaders(data.adminToken);

  group('admin read endpoints', () => {
    apiRequest('GET', '/api/admin/providers/pending', {
      headers,
      tags: { endpoint: '/api/admin/providers/pending' },
    });
    apiRequest('GET', '/api/admin/providers/approved', {
      headers,
      tags: { endpoint: '/api/admin/providers/approved' },
    });
    apiRequest('GET', '/api/admin/providers/rejected', {
      headers,
      tags: { endpoint: '/api/admin/providers/rejected' },
    });
    apiRequest('GET', '/api/admin/all-providers', {
      headers,
      tags: { endpoint: '/api/admin/all-providers' },
    });
    apiRequest('GET', '/api/admin/cities', {
      headers,
      tags: { endpoint: '/api/admin/cities' },
    });
  });

  if (INCLUDE_MUTATIONS) {
    group('admin mutation endpoints', () => {
      const providerDetailId = data.pendingProviderId || data.providerId;
      if (providerDetailId) {
        apiRequest('GET', `/api/admin/providers/${providerDetailId}`, {
          headers,
          tags: { endpoint: '/api/admin/providers/{id}' },
        });

        apiRequest('POST', `/api/admin/providers/${providerDetailId}/reject`, {
          headers,
          payload: { reason: maybe('REJECT_REASON') || 'Load testing rejection flow' },
          tags: { endpoint: '/api/admin/providers/{id}/reject' },
        });

        apiRequest('POST', `/api/admin/providers/${providerDetailId}/approve`, {
          headers,
          tags: { endpoint: '/api/admin/providers/{id}/approve' },
        });
      }

      apiRequest('POST', '/api/admin/cities', {
        headers,
        payload: {
          cityName: maybe('CITY_NAME') || `LoadCity-${__VU}-${Date.now()}`,
          state: maybe('SEARCH_STATE') || 'Maharashtra',
          pincodes: [
            {
              pincode: maybe('SEARCH_PINCODE') || '400001',
              areaName: maybe('SEARCH_AREA') || 'Load Area',
            },
          ],
        },
        tags: { endpoint: '/api/admin/cities' },
      });

      if (data.cityId) {
        apiRequest('POST', `/api/admin/cities/${data.cityId}/pincodes`, {
          headers,
          payload: [
            {
              pincode: maybe('EXTRA_PINCODE') || '400002',
              areaName: maybe('EXTRA_AREA') || 'Added by k6',
            },
          ],
          tags: { endpoint: '/api/admin/cities/{cityId}/pincodes' },
        });

        apiRequest('PATCH', `/api/admin/cities/${data.cityId}/status`, {
          headers,
          payload: { isActive: false },
          tags: { endpoint: '/api/admin/cities/{cityId}/status' },
        });

        apiRequest('DELETE', `/api/admin/cities/${data.cityId}`, {
          headers,
          tags: { endpoint: '/api/admin/cities/{cityId}' },
        });
      }

      if ((__ENV.ENABLE_PROVIDER_REGISTRATION || 'false') === 'true') {
        addProviderRegistrationHit();
      }
    });
  }

  sleep(PAUSE_SECONDS);
}

export function teardown(data) {
  const tokens = [data.customerToken, data.providerToken, data.adminToken].filter(Boolean);
  for (const token of tokens) {
    apiRequest('POST', '/api/logout', {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: 'application/json',
      },
      tags: { endpoint: '/api/logout' },
    });
  }
}


