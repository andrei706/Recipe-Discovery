import { useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { signup } from "../api/auth.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function SignupPage() {
  const navigate = useNavigate();
  const { saveAuth, token } = useAuth();
  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  if (token) {
    return <Navigate to="/" replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError("");

    const usernameRegex = /^[a-zA-Z0-9_]+$/;
    if (!usernameRegex.test(username)) {
      setError("Username can only contain letters, numbers, and underscores (_), without spaces.");
      return;
    }

    if (password !== confirmPassword) {
      setError("Passwords do not match");
      return;
    }

    setLoading(true);
    try {
      const response = await signup({ username, email, password });
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
      <h2>Sign up</h2>
      {error ? <div className="alert">{error}</div> : null}
      <form className="form-row" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Username"
          value={username}
          onChange={(event) => {
            setUsername(event.target.value);
            event.target.setCustomValidity("");
          }}
          onInvalid={(event) => {
            event.target.setCustomValidity("You can only use letters, numbers, and underscores.");
          }}
          maxLength={50}
          pattern="^[a-zA-Z0-9_]+$"
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(event) => setEmail(event.target.value)}
          maxLength={100}
          required
        />
        <input
          type="password"
          placeholder="Password"
          value={password}
          onChange={(event) => setPassword(event.target.value)}
          minLength={8}
          required
        />
        <input
          type="password"
          placeholder="Confirm password"
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          minLength={8}
          required
        />
        <button className="primary-btn" type="submit" disabled={loading}>
          {loading ? "Creating account..." : "Create account"}
        </button>
      </form>
      <p className="auth-switch">
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </div>
  );
}
