import { useEffect, useState } from "react";
import { getMe, logout } from "../api/auth";
import { useAuth } from "../context/AuthContext";

/**
 * Dashboard
 * - Accessible only to authenticated & verified users
 * - Confirms identity with backend (/me)
 */
export default function Dashboard() {
  const { setAuth } = useAuth();
  const [loading, setLoading] = useState(true);
  const [user, setUser] = useState(null);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    async function loadUser() {
      try {
        const data = await getMe();

        if (!active) return;

        if (!data || !data.authenticated) {
          // Session expired or invalid
          setAuth({
            loading: false,
            authenticated: false,
            emailVerified: false,
            email: null,
          });
          return;
        }

        setUser(data);
      } catch {
        if (!active) return;
        setError("Failed to load user information");
      } finally {
        if (active) setLoading(false);
      }
    }

    loadUser();

    return () => {
      active = false;
    };
  }, [setAuth]);

  async function handleLogout() {
    await logout();
    setAuth({
      loading: false,
      authenticated: false,
      emailVerified: false,
      email: null,
    });
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="text-gray-600">Loading dashboard...</span>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center text-red-600">
        {error}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-3xl mx-auto bg-white p-6 rounded-lg shadow">
        <h1 className="text-2xl font-semibold mb-2">
          Welcome
        </h1>

        <p className="text-gray-700 mb-6">
          Logged in as <strong>{user.email}</strong>
        </p>

        <button
          onClick={handleLogout}
          className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600"
        >
          Logout
        </button>
      </div>
    </div>
  );
}
