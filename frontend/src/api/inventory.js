import { apiRequest } from "./client.js";

export function addIngredient(token, { ingredientId, quantity }) {
  return apiRequest("/api/inventory/add", {
    method: "POST",
    token,
    params: { ingredientId, quantity }
  });
}

export function updateIngredient(token, { ingredientId, newQuantity }) {
  return apiRequest("/api/inventory/update", {
    method: "PUT",
    token,
    params: { ingredientId, newQuantity }
  });
}

export function removeIngredient(token, { ingredientId }) {
  return apiRequest("/api/inventory/remove", {
    method: "DELETE",
    token,
    params: { ingredientId }
  });
}

export function getInventory(token) {
  return apiRequest("/api/inventory", { token });
}
