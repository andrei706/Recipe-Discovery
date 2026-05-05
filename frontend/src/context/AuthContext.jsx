import { createContext, useContext, useMemo, useState } from "react";

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

  const saveAuth = (nextAuth) => {
    setAuth(nextAuth);
    if (nextAuth) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(nextAuth));
    } else {
      localStorage.removeItem(STORAGE_KEY);
    }
  };

  const value = useMemo(
    () => ({
      auth,
      token: auth?.token || null,
      user: auth?.user || null,
      saveAuth,
      clearAuth: () => saveAuth(null)
    }),
    [auth]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }
  return context;
}

