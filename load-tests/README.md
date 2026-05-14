# Load Test Guide

## Prerequisites
- Install k6: https://k6.io/docs/get-started/installation/
- Backend is running and seeded with valid `customerId` and `productId`
- A valid JWT token with permission to create orders

## Run
```bash
k6 run load-tests/order-transaction-load.js \
  -e BASE_URL=http://localhost:8080/api/v1 \
  -e TOKEN=your_jwt_here
```

## Notes
- Scenario ramps from 100 -> 2000 virtual users.
- Adjust payload `customerId`/`productId` in `order-transaction-load.js` for your environment.
- For production-like test, run against a staging environment with production DB pool/thread settings.
