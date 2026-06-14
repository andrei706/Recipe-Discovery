import { createContext, useContext, useMemo, useState, useEffect, useRef, useCallback } from "react";
import { refresh as refreshTokenApi } from "../api/auth.js";

const STORAGE_KEY = "rd_auth";

const AuthContext = createContext(null);

function readStoredAuth() {
  const raw = localStorage.getItem(STORAGE_KEY);
  if (!raw) {
    return null;
  }
  try {
    return JSON.parse(raw);
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState(readStoredAuth());

  const saveAuth = useCallback((nextAuth) => {
    // augment stored auth with a tokenObtainedAt timestamp if not present
    let toStore = nextAuth;
    if (nextAuth) {
      const now = Date.now();
      toStore = { ...nextAuth, tokenObtainedAt: nextAuth.tokenObtainedAt || now };
      localStorage.setItem(STORAGE_KEY, JSON.stringify(toStore));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
    setAuth(toStore);
  }, []);

  const clearAuth = useCallback(() => saveAuth(null), [saveAuth]);

  const value = useMemo(
    () => ({
      auth,
      token: auth?.token || null,
      user: auth?.user || null,
      saveAuth,
      clearAuth
    }),
    [auth, saveAuth, clearAuth]
  );

  // Background token refresh: track user activity and refresh token silently when needed
  const lastActivityRef = useRef(Date.now());
  const lastEventRef = useRef(0);

  useEffect(() => {
    function recordActivity() {
      const now = Date.now();
      // throttle updates to at most once per second
      if (now - lastEventRef.current > 1000) {
        lastEventRef.current = now;
        lastActivityRef.current = now;
      }
    }

    window.addEventListener("mousemove", recordActivity);
    window.addEventListener("click", recordActivity);
    window.addEventListener("keydown", recordActivity);

    return () => {
      window.removeEventListener("mousemove", recordActivity);
      window.removeEventListener("click", recordActivity);
      window.removeEventListener("keydown", recordActivity);
    };
  }, []);

  useEffect(() => {
    const handleAuthExpired = () => {
      clearAuth();
    };

    window.addEventListener("auth-expired", handleAuthExpired);

    return () => {
      window.removeEventListener("auth-expired", handleAuthExpired);
    };
  }, [clearAuth]);

  useEffect(() => {
    if (!auth || !auth.token || !auth.expiresInMs) {
      return;
    }

    const RECENT_ACTIVITY_MS = 5 * 60 * 1000; // 5 minutes
    const CHECK_INTERVAL_MS = 30 * 1000; // 30 seconds

    let cancelled = false;

    const intervalId = setInterval(async () => {
      try {
        if (cancelled) return;
        const obtainedAt = auth.tokenObtainedAt || Date.now();
        const tokenAge = Date.now() - obtainedAt;
        const halfLife = (auth.expiresInMs || 0) * 0.5;

        // Only attempt refresh if token reached 50% of its lifetime and user was active recently
        if (auth.expiresInMs && tokenAge >= halfLife && (Date.now() - lastActivityRef.current) <= RECENT_ACTIVITY_MS) {
          try {
            const resp = await refreshTokenApi(auth.token);
            // update auth state with new token
            saveAuth({
              token: resp.token,
              tokenType: resp.tokenType,
              expiresInMs: resp.expiresInMs,
              user: {
                userId: resp.userId,
                username: resp.username,
                email: resp.email
              }
            });
          } catch (err) {
            // If refresh fails due to unauthorized/invalid token, clear auth to force re-login
            const msg = (err?.message || "").toLowerCase();
            if (msg.includes("unauthorized") || msg.includes("invalid")) {
              saveAuth(null);
            } else {
              // otherwise just log the error and try again later
              // eslint-disable-next-line no-console
              console.error("Token refresh failed:", err);
            }
          }
        }
      } catch (err) {
        // eslint-disable-next-line no-console
        console.error("Unexpected error in token refresh loop:", err);
      }
    }, CHECK_INTERVAL_MS);

    return () => {
      cancelled = true;
      clearInterval(intervalId);
    };
  }, [auth, saveAuth]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

