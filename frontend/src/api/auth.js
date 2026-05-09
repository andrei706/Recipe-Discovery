import { apiRequest } from "./client.js";

export function login({ loginType, identifier, password }) {
  return apiRequest("/api/auth/login", {
    method: "POST",
    body: { loginType, identifier, password }
  });
}

export function signup({ username, email, password }) {
  return apiRequest("/api/auth/signup", {
    method: "POST",
    body: { username, email, password }
  });
}

export function logout(token) {
  return apiRequest("/api/auth/logout", {
    method: "POST",
    token
  });
}

export function changePassword(token, { currentPassword, newPassword }) {
  return apiRequest("/api/auth/change-password", {
    method: "POST",
    token,
    body: { currentPassword, newPassword }
  });
}

