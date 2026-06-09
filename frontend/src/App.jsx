import { Navigate, Route, Routes } from "react-router-dom";
import TopBar from "./components/TopBar.jsx";
import LoginPage from "./pages/LoginPage.jsx";
import SignupPage from "./pages/SignupPage.jsx";
import RecipesPage from "./pages/RecipesPage.jsx";
import RecipePage from "./pages/RecipePage.jsx";
import InventoryPage from "./pages/InventoryPage.jsx";
import ProfilePage from "./pages/ProfilePage.jsx";
import AIChefPage from "./pages/AIChefPage.jsx";
import PlannerPage from "./pages/PlannerPage.jsx";
import ProtectedRoute from "./routes/ProtectedRoute.jsx";

export default function App() {
  return (
    <div className="app-shell">
      <TopBar />
      <div className="app-content">
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/signup" element={<SignupPage />} />
          <Route element={<ProtectedRoute />}>
            <Route path="/" element={<RecipesPage />} />
            <Route path="/recipe/:recipeId" element={<RecipePage />} />
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

