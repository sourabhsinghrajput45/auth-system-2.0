import { useEffect, useState } from "react";
import { getMe, logout } from "../api/auth";
import { useAuth } from "../context/AuthContext";

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
          setAuth({
            loading: false,
            authenticated: false,
            email: null,
          });
          return;
        }

        // 🔥 SINGLE SOURCE OF TRUTH
        setUser(data);
      } catch {
        if (active) setError("Failed to load user information");
      } finally {
        if (active) setLoading(false);
      }
    }

    loadUser();
    return () => {
      active = false;
    };
  }, [setAuth]);

  function handleLogout() {
    logout();
    setAuth({
      loading: false,
      authenticated: false,
      email: null,
    });
  }

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        Loading dashboard…
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

  const isVerified = user.emailVerified === true;

  return (
    <div className="min-h-screen bg-gray-100 p-8">
      <div className="max-w-3xl mx-auto bg-white p-6 rounded-lg shadow space-y-6">
        <h1 className="text-2xl font-semibold">Welcome</h1>

        <p className="text-gray-700">
          Logged in as <strong>{user.email}</strong>
        </p>

        <div
          className={`p-4 rounded-md border ${
            isVerified
              ? "bg-green-50 border-green-300 text-green-800"
              : "bg-yellow-50 border-yellow-300 text-yellow-800"
          }`}
        >
          {isVerified ? (
            <>
              <p className="font-medium">Email verified</p>
              <p className="text-sm mt-1">
                Your email is verified. All features are accessible.
              </p>
            </>
          ) : (
            <>
              <p className="font-medium">Email not verified</p>
              <p className="text-sm mt-1">
                Please verify your email.  
                Log out and log in again after verification.
              </p>
            </>
          )}
        </div>

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
