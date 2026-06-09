import { useEffect, useState } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import TopBar from "./components/TopBar.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";
import RecipesPage from "./pages/RecipesPage.jsx";
import RecipePage from "./pages/RecipePage.jsx";
import RecipeInstructionsPage from "./pages/RecipeInstructionsPage.jsx";
import InventoryPage from "./pages/InventoryPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import AIChefPage from "./pages/AIChefPage.jsx";
import PlannerPage from "./pages/PlannerPage.jsx";
import ProtectedRoute from "./routes/ProtectedRoute.jsx";

const THEME_STORAGE_KEY = "rd_theme";

export default function App() {
  const [theme, setTheme] = useState(() => {
    return localStorage.getItem(THEME_STORAGE_KEY) === "dark" ? "dark" : "light";
  });

  useEffect(() => {
    document.documentElement.dataset.theme = theme;
    localStorage.setItem(THEME_STORAGE_KEY, theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((currentTheme) => (currentTheme === "dark" ? "light" : "dark"));
  };

  return (
    <div className="app-shell">
      <TopBar theme={theme} onToggleTheme={toggleTheme} />
      <div className="app-content">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<RecipesPage />} />
            <Route path="/recipe/:recipeId" element={<RecipePage />} />
            <Route path="/recipe/:recipeId/instructions" element={<RecipeInstructionsPage />} />
            <Route path="/inventory" element={<InventoryPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="/ai-chef" element={<AIChefPage />} />
            <Route path="/planner" element={<PlannerPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </div>
  );
}

