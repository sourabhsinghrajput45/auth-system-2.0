const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

/**
 * Login user
 * - Sends credentials
 * - Receives HTTP-only cookie
 */
export async function login(email, password) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: "POST",
    credentials: "include", // REQUIRED for cookies
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    throw new Error("Login failed");
  }

  return response.json();
}

/**
 * Check authentication status
 * Used on app load
 */
export async function getAuthStatus() {
  const response = await fetch(`${API_BASE_URL}/auth/status`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    return {
      authenticated: false,
      emailVerified: false,
    };
  }

  return response.json();
}

/**
 * Fetch current user info
 * Protected endpoint
 */
export async function getMe() {
  const response = await fetch(`${API_BASE_URL}/me`, {
    method: "GET",
    credentials: "include",
  });

  if (!response.ok) {
    return null;
  }

  return response.json();
}

/**
 * Logout user
 * Clears cookie on backend
 */
export async function logout() {
  await fetch(`${API_BASE_URL}/auth/logout`, {
    method: "POST",
    credentials: "include",
  });
}
