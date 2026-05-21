import { apiRequest } from "./client.js";

export function sendChatPrompt(token, prompt) {
  return apiRequest("/api/ai/chat", {
    method: "POST",
    token,
    body: { prompt }
  });
}
