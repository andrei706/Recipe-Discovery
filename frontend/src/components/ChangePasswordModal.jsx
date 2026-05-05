import { useState } from "react";
import { changePassword } from "../api/auth.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function ChangePasswordModal({ onClose }) {
  const { token } = useAuth();
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [status, setStatus] = useState({ type: "", message: "" });
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (event) => {
    event.preventDefault();
    setLoading(true);
    setStatus({ type: "", message: "" });
    try {
      const response = await changePassword(token, {
        currentPassword,
        newPassword
      });
      setStatus({ type: "success", message: response?.message || "Password updated." });
      setCurrentPassword("");
      setNewPassword("");
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true">
      <div className="modal">
        <div className="section-header">
          <h3>Change password</h3>
          <button type="button" className="secondary-btn" onClick={onClose}>
            Close
          </button>
        </div>
        {status.message ? (
          <div className={status.type === "success" ? "success" : "alert"}>{status.message}</div>
        ) : null}
        <form className="form-row" onSubmit={handleSubmit}>
          <input
            type="password"
            placeholder="Current password"
            value={currentPassword}
            onChange={(event) => setCurrentPassword(event.target.value)}
            required
          />
          <input
            type="password"
            placeholder="New password"
            value={newPassword}
            onChange={(event) => setNewPassword(event.target.value)}
            required
          />
          <button type="submit" className="primary-btn" disabled={loading}>
            {loading ? "Saving..." : "Update password"}
          </button>
        </form>
      </div>
    </div>
  );
}

