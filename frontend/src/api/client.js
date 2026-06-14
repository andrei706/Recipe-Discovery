const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8081";

export async function apiRequest(path, { method = "GET", token, body, params } = {}) {
  const url = new URL(`${API_BASE_URL}${path}`);
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null) {
        url.searchParams.set(key, String(value));
      }
    });
  }

  const response = await fetch(url.toString(), {
    method,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {})
    },
    body: body ? JSON.stringify(body) : undefined
  });

  if (response.status === 401 && typeof window !== "undefined") {
    window.dispatchEvent(new Event("auth-expired"));
  }

  const contentType = response.headers.get("content-type") || "";
  let payload;
  if (contentType.includes("application/json")) {
    payload = await response.json();
  } else {
    payload = await response.text();
  }

  if (!response.ok) {
    let message = "Request failed";
    if (typeof payload === "string" && payload) {
      message = payload;
    } else if (payload?.message) {
      message = payload.message;
    } else if (payload?.error) {
      message = payload.error;
    }
    if (response.status === 401) {
      message = "Unauthorized";
    }
    throw new Error(message);
  }

  return payload;
}

