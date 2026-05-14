import http from "k6/http";
import { check, sleep } from "k6";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080/api/v1";
const TOKEN = __ENV.TOKEN || "";

export const options = {
  scenarios: {
    transaction_spike: {
      executor: "ramping-vus",
      startVUs: 100,
      stages: [
        { duration: "1m", target: 500 },
        { duration: "2m", target: 1000 },
        { duration: "2m", target: 1500 },
        { duration: "2m", target: 2000 },
        { duration: "1m", target: 0 }
      ],
      gracefulRampDown: "30s"
    }
  },
  thresholds: {
    http_req_failed: ["rate<0.05"],
    http_req_duration: ["p(95)<1200", "p(99)<2500"]
  }
};

function buildPayload() {
  return JSON.stringify({
    shippingAddress: "Load test address",
    notes: "k6 transaction load test",
    paymentMethod: "CASH",
    customerId: 1,
    items: [
      { productId: 1, quantity: 1 }
    ]
  });
}

export default function () {
  const headers = {
    "Content-Type": "application/json",
    Authorization: TOKEN ? `Bearer ${TOKEN}` : ""
  };

  const res = http.post(`${BASE_URL}/orders`, buildPayload(), { headers });
  check(res, {
    "status is 2xx/4xx": (r) => r.status >= 200 && r.status < 500
  });

  sleep(0.1);
}
