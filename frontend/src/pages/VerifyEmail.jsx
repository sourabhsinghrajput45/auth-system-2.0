import { useEffect, useState } from "react";
import { getAuthStatus } from "../api/auth";
import { useAuth } from "../context/AuthContext";

/**
 * VerifyEmail page
 * - Shown when user is authenticated but email is not verified
 * - Polls backend status
 * - Updates ONLY verification flag (PROD SAFE)
 */
export default function VerifyEmail() {
  const { setAuth } = useAuth();
  const [checking, setChecking] = useState(false);

  useEffect(() => {
    let active = true;

    async function checkVerification() {
      setChecking(true);
      try {
        const data = await getAuthStatus();
        if (!active) return;

        // ✅ Only flip verification flag
        if (data.authenticated && data.emailVerified) {
          setAuth((prev) => ({
            ...prev,
            loading: false,
            emailVerified: true,
          }));
        }
      } finally {
        if (active) setChecking(false);
      }
    }

    // initial check
    checkVerification();

    const interval = setInterval(checkVerification, 5000);

    return () => {
      active = false;
      clearInterval(interval);
    };
  }, [setAuth]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full bg-white p-8 rounded-lg shadow-md text-center space-y-4">
        <h1 className="text-xl font-semibold">
          Email verification required
        </h1>

        <p className="text-gray-600">
          Please verify your email to continue.
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
