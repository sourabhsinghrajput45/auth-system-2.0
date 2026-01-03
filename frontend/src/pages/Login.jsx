import { useState } from "react";
import { login } from "../api/auth";
import { useAuth } from "../context/AuthContext";

export default function Login() {
  const { setAuth } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  async function handleSubmit(e) {
    e.preventDefault();
    setError("");
    setSubmitting(true);

    try {
      const data = await login(email, password);

      setAuth({
        loading: false,
        authenticated: true,
        emailVerified: Boolean(data.emailVerified),
        email: data.email ?? null,
      });
    } catch {
      setError("Invalid email or password");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-100">
      <form
        onSubmit={handleSubmit}
        className="w-full max-w-sm bg-white p-8 rounded-lg shadow-md"
      >
        <h1 className="text-2xl font-semibold text-center mb-6">
          Sign in
        </h1>

        {error && (
          <div className="mb-4 text-sm text-red-600 text-center">
            {error}
          </div>
        )}

        <div className="mb-4">
          <label className="block text-sm text-gray-700 mb-1">
            Email
          </label>
          <input
            type="email"
            required
            autoComplete="email"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring focus:border-black"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </div>

        <div className="mb-6">
          <label className="block text-sm text-gray-700 mb-1">
            Password
          </label>
          <input
            type="password"
            required
            autoComplete="current-password"
            className="w-full border rounded px-3 py-2 focus:outline-none focus:ring focus:border-black"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />
        </div>

        <button
          type="submit"
          disabled={submitting}
          className="w-full bg-black text-white py-2 rounded hover:bg-gray-800 disabled:opacity-50"
        >
          {submitting ? "Signing in..." : "Sign in"}
        </button>
      </form>
    </div>
  );
}
