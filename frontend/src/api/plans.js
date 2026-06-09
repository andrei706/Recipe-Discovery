import { apiRequest } from "./client.js";

// ── Plans ────────────────────────────────────────────────────────────────────

export function getPlans(token) {
  return apiRequest("/api/plans", { token });
}

export function createPlan(token, body) {
  return apiRequest("/api/plans", { method: "POST", token, body });
}

export function updatePlan(token, planId, body) {
  return apiRequest(`/api/plans/${planId}`, { method: "PUT", token, body });
}

export function deletePlan(token, planId) {
  return apiRequest(`/api/plans/${planId}`, { method: "DELETE", token });
}

// ── Plan Details ─────────────────────────────────────────────────────────────

export function getPlanDetails(token, planId) {
  return apiRequest(`/api/plans/${planId}/details`, { token });
}

export function addPlanDetail(token, planId, body) {
  return apiRequest(`/api/plans/${planId}/details`, { method: "POST", token, body });
}

export function updatePlanDetail(token, detailId, isFollowed) {
  return apiRequest(`/api/plans/details/${detailId}`, {
    method: "PUT",
    token,
    params: { isFollowed },
  });
}

export function deletePlanDetail(token, detailId) {
  return apiRequest(`/api/plans/details/${detailId}`, { method: "DELETE", token });
}
