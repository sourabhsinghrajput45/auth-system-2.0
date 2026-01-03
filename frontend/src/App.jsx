import { AuthProvider, useAuth } from "./context/AuthContext";

import Login from "./pages/Login";
import VerifyEmail from "./pages/VerifyEmail";
import Dashboard from "./pages/Dashboard";

/**
 * Root router driven by backend auth state
 */
function Router() {
  const { auth } = useAuth();

  // Still determining auth state
  if (auth.loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <span className="text-gray-600">Loading...</span>
      </div>
    );
  }

  // Not authenticated → Login
  if (!auth.authenticated) {
    return <Login />;
  }

  // Authenticated but email not verified
  if (!auth.emailVerified) {
    return <VerifyEmail />;
  }

  // Authenticated + verified
  return <Dashboard />;
}

export default function App() {
  return (
    <AuthProvider>
      <Router />
    </AuthProvider>
  );
}
