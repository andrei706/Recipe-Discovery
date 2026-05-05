import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { logout as logoutApi } from "../api/auth.js";

export default function TopBar() {
  const { token, user, clearAuth } = useAuth();
  const navigate = useNavigate();

  const handleLogout = async () => {
    if (token) {
      try {
        await logoutApi(token);
      } catch {
        // Ignore logout errors; token is cleared client-side.
      }
    }
    clearAuth();
    navigate("/login");
  };

  return (
    <header className="topbar">
      <div className="brand">Recipe Discovery</div>
      <nav>
        {token ? (
          <>
            <NavLink to="/">Recipes</NavLink>
            <NavLink to="/inventory">Inventory</NavLink>
            <NavLink to="/profile">Profile</NavLink>
            <span>{user?.username || "User"}</span>
            <button type="button" onClick={handleLogout}>
              Logout
            </button>
          </>
        ) : (
          <NavLink to="/login">Login</NavLink>
        )}
      </nav>
    </header>
  );
}

