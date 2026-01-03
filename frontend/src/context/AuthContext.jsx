import { createContext, useContext, useEffect, useState } from "react";
import { getAuthStatus } from "../api/auth";

/**
 * AuthContext
 * - Holds authentication state
 * - Syncs state with backend (/auth/status)
 */
const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [auth, setAuth] = useState({
    loading: true,
    authenticated: false,
    emailVerified: false,
    email: null,
  });

  /**
   * On app load:
   * - Ask backend for auth status
   * - Backend decides (cookie-based)
   */
  useEffect(() => {
    let active = true;

    async function checkAuth() {
      try {
        const data = await getAuthStatus();

        if (!active) return;

        setAuth({
          loading: false,
          authenticated: Boolean(data.authenticated),
          emailVerified: Boolean(data.emailVerified),
          email: data.email ?? null,
        });
      } catch {
        if (!active) return;

        setAuth({
          loading: false,
          authenticated: false,
          emailVerified: false,
          email: null,
        });
      }
    }

    checkAuth();

    return () => {
      active = false;
    };
  }, []);

  return (
    <AuthContext.Provider value={{ auth, setAuth }}>
      {children}
    </AuthContext.Provider>
  );
}

/**
 * Hook to access auth state
 */
export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used inside AuthProvider");
  }

  return context;
}
