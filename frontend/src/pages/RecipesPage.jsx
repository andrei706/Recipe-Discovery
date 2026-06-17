import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import DietFilter from "../components/DietFilter.jsx";
import RecipeCard from "../components/RecipeCard.jsx";
import { getAvailableRecipes, getMatchRecipes, getRecipeDetails } from "../api/recipes.js";
import { useAuth } from "../context/AuthContext.jsx";

const VIEW_OPTIONS = [
  { id: "available", label: "Available" },
  { id: "match", label: "Match %" }
];

export default function RecipesPage() {
  const { token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [view, setView] = useState("available");
  const [dietFilters, setDietFilters] = useState([]);
  const [recipes, setRecipes] = useState([]);
  const [detailsMap, setDetailsMap] = useState({});
  const [status, setStatus] = useState({ type: "", message: "" });
  const [loading, setLoading] = useState(false);
  const [recipeSearch, setRecipeSearch] = useState("");

  useEffect(() => {
    if (location.state?.status) {
      setStatus(location.state.status);
    }
    if (Array.isArray(location.state?.dietFilters)) {
      setDietFilters(location.state.dietFilters);
    }
    if (location.state?.status || location.state?.dietFilters) {
      window.history.replaceState({}, document.title);
    }
  }, [location.state]);

  const loadRecipes = async () => {
    setLoading(true);
    setStatus({ type: "", message: "" });
    try {
      let response = [];
      if (view === "available") {
        response = await getAvailableRecipes(token);
      } else {
        response = await getMatchRecipes(token);
      }
      setRecipes(response || []);
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    } finally {
      setLoading(false);
    }
  };

  const loadDetails = async () => {
    try {
      const response = await getRecipeDetails(token);
      const map = (response || []).reduce((acc, item) => {
        acc[item.recipe.recipeId] = item;
        return acc;
      }, {});
      setDetailsMap(map);
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    }
  };

  useEffect(() => {
    loadRecipes();
    loadDetails();
  }, [view]);

  const normalizedRecipes = useMemo(() => {
    return (recipes || []).map((item) => {
      if (item?.recipe) {
        return {
          recipe: item.recipe,
          match: {
            matchedIngredients: item.matchedIngredients,
            totalIngredients: item.totalIngredients,
            matchPercentage: item.matchPercentage
          }
        };
      }
      return { recipe: item, match: null };
    });
  }, [recipes]);

  const filtered = useMemo(() => {
    let result = normalizedRecipes;
    if (dietFilters.length > 0) {
      result = result.filter(({ recipe }) => {
        const dietNames = (recipe?.dietClassifications || []).map((diet) => diet.name?.toLowerCase());
        return dietFilters.every((dietFilter) => dietNames.includes(dietFilter.toLowerCase()));
      });
    }
    if (recipeSearch) {
      const q = recipeSearch.trim().toLowerCase();
      result = result.filter(({ recipe }) => {
        const matchesName = (recipe?.name || "").toLowerCase().includes(q);
        const matchesDescription = (recipe?.description || "").toLowerCase().includes(q);
        const ingredients = detailsMap[recipe?.recipeId]?.ingredients || [];
        const matchesIngredients = ingredients.some((ing) =>
          (ing.ingredientName || "").toLowerCase().includes(q)
        );
        return matchesName || matchesDescription || matchesIngredients;
      });
    }
    return result;
  }, [dietFilters, recipeSearch, normalizedRecipes, detailsMap]);

  const handleCook = (recipeId) => {
    navigate(`/recipe/${recipeId}`);
  };

  const handleDietClick = (dietName) => {
    setDietFilters([dietName]);
    navigate("/", { replace: true });
  };

  return (
    <div className="split-layout recipes-layout">
      <div className="grid recipes-results-panel" style={{ gap: 16 }}>
        <div className="section-header">
          <h2>Recipes</h2>
          <div className="form-row view-toggle-row">
            {VIEW_OPTIONS.map((option) => (
              <button
                key={option.id}
                type="button"
                className={view === option.id ? "primary-btn" : "secondary-btn"}
                onClick={() => setView(option.id)}
              >
                {option.label}
              </button>
            ))}
          </div>
        </div>

        <div className="card">
          <div className="form-row">
            <label style={{ fontWeight: 600 }}>Search Recipes:</label>
            <input
              type="text"
              className="search-input"
              placeholder="Search by name..."
              value={recipeSearch}
              onChange={(e) => setRecipeSearch(e.target.value)}
            />
          </div>
        </div>

        {status.message ? (
          <div className={status.type === "success" ? "success" : "alert"}>{status.message}</div>
        ) : null}
        {loading ? <div className="card">Loading recipes...</div> : null}

        {!loading && filtered.length === 0 ? (
          <div className="card" style={{ textAlign: "center", padding: "32px 16px", color: "#6b7280" }}>
            <div style={{ fontSize: 24, marginBottom: 8 }}>🍳</div>
            <h3 style={{ margin: "0 0 8px 0", color: "#1f2937" }}>No recipes found</h3>
            <p style={{ margin: 0, fontSize: 14 }}>Try adjusting your search filters or adding more ingredients to your inventory.</p>
          </div>
        ) : (
          <div className="grid grid-3">
            {filtered.map(({ recipe, match }) => (
              <RecipeCard
                key={recipe.recipeId || recipe.name}
                recipe={recipe}
                match={match}
                details={detailsMap[recipe.recipeId]}
                onCook={() => handleCook(recipe.recipeId)}
                onDietClick={handleDietClick}
              />
            ))}
          </div>
        )}
      </div>

      <div className="grid recipes-filter-panel" style={{ gap: 16 }}>
        <DietFilter value={dietFilters} onChange={setDietFilters} />
      </div>
    </div>
  );
}
