import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { login } from "../api/auth.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function LoginPage() {
  const navigate = useNavigate();
  const { saveAuth, token } = useAuth();
  const [identifier, setIdentifier] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (token) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");
    setLoading(true);
    try {
      const computedLoginType = identifier.includes("@") ? "EMAIL" : "USERNAME";
      const response = await login({ loginType: computedLoginType, identifier, password });
      saveAuth({
        token: response.token,
        tokenType: response.tokenType,
        expiresInMs: response.expiresInMs,
        user: {
          userId: response.userId,
          username: response.username,
          email: response.email
        }
      });
      navigate("/");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="card auth-card">
      <h2>Login</h2>
      {error ? <div className="alert">{error}</div> : null}
      <form className="form-row" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Username or Email"
          value={identifier}
          onChange={(event) => setIdentifier(event.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          required
        />
        <button className="primary-btn" type="submit" disabled={loading}>
          {loading ? "Signing in..." : "Sign in"}
        </button>
      </form>
      <p className="auth-switch">
        Don't have an account? <Link to="/signup">Sign up</Link>
      </p>
    </div>
  );
}
