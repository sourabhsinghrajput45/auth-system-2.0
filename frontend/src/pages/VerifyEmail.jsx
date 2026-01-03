import { useEffect, useState } from "react";
import { getAuthStatus } from "../api/auth";
import { useAuth } from "../context/AuthContext";

/**
 * VerifyEmail page
 * - Shown when user is logged in but email is not verified
 * - Periodically checks backend status
 */
export default function VerifyEmail() {
  const { setAuth } = useAuth();
  const [checking, setChecking] = useState(false);
  const [message, setMessage] = useState(
    "Please verify your email to continue."
  );

  useEffect(() => {
    let active = true;

    async function checkVerification() {
      setChecking(true);
      try {
        const data = await getAuthStatus();

        if (!active) return;

        if (data.emailVerified) {
          setAuth({
            loading: false,
            authenticated: true,
            emailVerified: true,
            email: data.email ?? null,
          });
        }
      } finally {
        if (active) setChecking(false);
      }
    }

    const interval = setInterval(checkVerification, 5000);

    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [setAuth]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md text-center">
        <h1 className="text-xl font-semibold mb-4">
          Email verification required
        </h1>

        <p className="text-gray-600 mb-6">
          {message}
        </p>

        <p className="text-sm text-gray-500">
          {checking
            ? "Checking verification status..."
            : "Once verified, this page will update automatically."}
        </p>
      </div>
    </div>
  );
}
