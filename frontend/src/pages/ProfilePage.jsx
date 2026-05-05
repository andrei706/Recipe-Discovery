import { useState } from "react";
import ChangePasswordModal from "../components/ChangePasswordModal.jsx";
import { useAuth } from "../context/AuthContext.jsx";

export default function ProfilePage() {
  const { user } = useAuth();
  const [showModal, setShowModal] = useState(false);

  return (
    <div className="grid" style={{ gap: 16, maxWidth: 720 }}>
      <div className="card">
        <h2>Profile</h2>
        <div className="form-row">
          <div>Username: {user?.username || "-"}</div>
          <div>Email: {user?.email || "-"}</div>
          <button type="button" className="primary-btn" onClick={() => setShowModal(true)}>
            Change password
          </button>
        </div>
      </div>
      {showModal ? <ChangePasswordModal onClose={() => setShowModal(false)} /> : null}
    </div>
  );
}

