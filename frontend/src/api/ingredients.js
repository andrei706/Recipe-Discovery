import { apiRequest } from "./client.js";

export function getIngredients(token) {
  return apiRequest("/api/ingredients", { token });
}

