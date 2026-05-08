import { apiRequest } from "./client.js";

export function getAvailableRecipes(token) {
  return apiRequest("/api/recipes/available", { token });
}

export function getSortedRecipes(token) {
  return apiRequest("/api/recipes/sorted-by-match", { token });
}

export function getMatchRecipes(token) {
  return apiRequest("/api/recipes/match-percentage", { token });
}

export function cookRecipe(token, recipeId) {
  return apiRequest(`/api/recipes/${recipeId}/cook`, {
    method: "POST",
    token
  });
}

export function getRecipeDetails(token) {
  return apiRequest("/api/recipes/details", { token });
}

export function getRecipeById(token, recipeId) {
  return apiRequest(`/api/recipes/${recipeId}`, { token });
}
