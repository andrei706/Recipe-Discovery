import { useEffect, useState } from "react";
import { NavLink, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";
import { logout as logoutApi } from "../api/auth.js";

function NavIcon({ name }) {
  const paths = {
    menu: (
      <>
        <path d="M4 7h16" />
        <path d="M4 12h16" />
        <path d="M4 17h16" />
      </>
    ),
    close: (
      <>
        <path d="M6 6l12 12" />
        <path d="M18 6L6 18" />
      </>
    ),
    recipes: (
      <>
        <path d="M5 6h14" />
        <path d="M5 12h14" />
        <path d="M5 18h10" />
      </>
    ),
    inventory: (
      <>
        <path d="M4 7l8-4 8 4-8 4-8-4Z" />
        <path d="M4 7v10l8 4 8-4V7" />
        <path d="M12 11v10" />
      </>
    ),
    chef: (
      <>
        <path d="M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8L12 3Z" />
        <path d="M5 18h14" />
      </>
    ),
    planner: (
      <>
        <path d="M6 4v4" />
        <path d="M18 4v4" />
        <path d="M4 8h16" />
        <path d="M5 6h14v14H5z" />
      </>
    ),
    profile: (
      <>
        <path d="M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8Z" />
        <path d="M5 21a7 7 0 0 1 14 0" />
      </>
    ),
    sun: (
      <>
        <path d="M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8Z" />
        <path d="M12 2v2" />
        <path d="M12 20v2" />
        <path d="M4.9 4.9l1.4 1.4" />
        <path d="M17.7 17.7l1.4 1.4" />
        <path d="M2 12h2" />
        <path d="M20 12h2" />
        <path d="M4.9 19.1l1.4-1.4" />
        <path d="M17.7 6.3l1.4-1.4" />
      </>
    ),
    moon: <path d="M20 15.5A8.5 8.5 0 0 1 8.5 4 7 7 0 1 0 20 15.5Z" />,
    login: (
      <>
        <path d="M10 17l5-5-5-5" />
        <path d="M15 12H3" />
        <path d="M14 4h5v16h-5" />
      </>
    ),
    logout: (
      <>
        <path d="M14 17l5-5-5-5" />
        <path d="M19 12H7" />
        <path d="M10 4H5v16h5" />
      </>
    )
  };

  return (
    <svg className={`nav-icon nav-icon-${name}`} viewBox="0 0 24 24" aria-hidden="true">
      <g fill="none" stroke="currentColor" strokeLinecap="round" strokeLinejoin="round" strokeWidth="2">
        {paths[name]}
      </g>
    </svg>
  );
}

export default function TopBar({ theme, onToggleTheme }) {
  const { token, user, clearAuth } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navId = "main-navigation";

  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  const closeMobileMenu = () => {
    setIsMobileMenuOpen(false);
  };

  const handleLogout = async () => {
    const confirmLogout = window.confirm("Are you sure you want to log out?");
    if (!confirmLogout) return;

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
    <header className={`topbar ${isMobileMenuOpen ? "menu-open" : ""}`}>
      <div className="topbar-main">
        <div className="brand">Recipe Discovery</div>
        <button
          type="button"
          className="mobile-menu-btn"
          aria-label={isMobileMenuOpen ? "Close navigation menu" : "Open navigation menu"}
          aria-controls={navId}
          aria-expanded={isMobileMenuOpen}
          onClick={() => setIsMobileMenuOpen((current) => !current)}
        >
          <NavIcon name={isMobileMenuOpen ? "close" : "menu"} />
        </button>
      </div>
      <nav id={navId} className={`topbar-nav ${isMobileMenuOpen ? "open" : ""}`} aria-label="Main navigation">
        {token ? (
          <>
            <NavLink to="/" onClick={closeMobileMenu}>
              <NavIcon name="recipes" />
              <span className="nav-label">Recipes</span>
            </NavLink>
            <NavLink to="/inventory" onClick={closeMobileMenu}>
              <NavIcon name="inventory" />
              <span className="nav-label">Inventory</span>
            </NavLink>
            <NavLink to="/ai-chef" onClick={closeMobileMenu}>
              <NavIcon name="chef" />
              <span className="nav-label">AI Chef</span>
            </NavLink>
            <NavLink to="/planner" onClick={closeMobileMenu}>
              <NavIcon name="planner" />
              <span className="nav-label">Meal Planner</span>
            </NavLink>
            <NavLink to="/profile" onClick={closeMobileMenu}>
              <NavIcon name="profile" />
              <span className="nav-label">Profile</span>
            </NavLink>
            <span className="nav-user">
              <NavIcon name="profile" />
              <span className="nav-label">{user?.username || "User"}</span>
            </span>
            <button
              type="button"
              className="theme-toggle-btn"
              aria-pressed={theme === "dark"}
              onClick={onToggleTheme}
            >
              <NavIcon name={theme === "dark" ? "sun" : "moon"} />
              <span className="nav-label">{theme === "dark" ? "Light mode" : "Dark mode"}</span>
            </button>
            <button type="button" onClick={handleLogout}>
              <NavIcon name="logout" />
              <span className="nav-label">Logout</span>
            </button>
          </>
        ) : (
          <>
            <button
              type="button"
              className="theme-toggle-btn"
              aria-pressed={theme === "dark"}
              onClick={onToggleTheme}
            >
              <NavIcon name={theme === "dark" ? "sun" : "moon"} />
              <span className="nav-label">{theme === "dark" ? "Light mode" : "Dark mode"}</span>
            </button>
            <NavLink to="/login" onClick={closeMobileMenu}>
              <NavIcon name="login" />
              <span className="nav-label">Login</span>
            </NavLink>
          </>
        )}
      </nav>
    </header>
  );
}

