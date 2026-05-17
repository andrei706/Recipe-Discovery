import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { cookRecipe, getRecipeById } from "../api/recipes.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function RecipePage() {
  const { recipeId } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState({ type: "", message: "" });

  useEffect(() => {
    getRecipeById(token, recipeId)
      .then(setData)
      .catch((err) => setStatus({ type: "error", message: err.message }))
      .finally(() => setLoading(false));
  }, [recipeId]);

  const handleCook = async () => {
    const confirmCook = window.confirm("Are you sure you want to cook this recipe? The required ingredients will be deducted from your inventory.");
    if (!confirmCook) return;

    setStatus({ type: "", message: "" });
    try {
      await cookRecipe(token, recipeId);
      setStatus({ type: "success", message: "Recipe has been cooked successfully! Ingredients have been deducted from inventory." });
      setTimeout(() => {
        navigate("/", { state: { status: { type: "success", message: "Recipe has been cooked successfully! Ingredients have been deducted from inventory." } } });
      }, 1500);
    } catch (err) {
      setStatus({ type: "error", message: err.message });
    }
  };

  if (loading) return <div className="card">Loading...</div>;
  if (!data) return null;

  const { recipe, matchedIngredients, totalIngredients, matchPercentage, ingredients } = data;
  const dietNames = recipe.dietClassifications?.map((d) => d.name).join(", ");
  const haveList = (ingredients || []).filter((i) => i.missingQuantity === 0);
  const missingList = (ingredients || []).filter((i) => i.missingQuantity > 0);

  return (
    <div style={{ maxWidth: 1000, margin: "0 auto" }}>
      <div className="section-header">
        <h2 style={{ margin: 0 }}>{recipe.name}</h2>
        <span className="badge">{matchPercentage}% match</span>
      </div>

      {status.message ? (
        <div className={status.type === "success" ? "success" : "alert"} style={{ marginBottom: 16 }}>
          {status.message}
        </div>
      ) : null}

      <div className="recipe-details-layout">
        {/* Coloana Stângă: Info Nutriționale, Ingrediente și Acțiuni */}
        <div className="grid" style={{ gap: 16 }}>
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Nutritional Information</h3>
            <div className="form-row">
              {recipe.totalPrepTimeMinutes ? <div>Prep time: {recipe.totalPrepTimeMinutes} min</div> : null}
              {recipe.caloriesKcal ? <div>Calories: {recipe.caloriesKcal} kcal</div> : null}
              {recipe.proteinsG ? <div>Protein: {recipe.proteinsG} g</div> : null}
              {recipe.carbohydratesG ? <div>Carbs: {recipe.carbohydratesG} g</div> : null}
              {recipe.fatsG ? <div>Fats: {recipe.fatsG} g</div> : null}
              {dietNames ? <div>Diets: {dietNames}</div> : null}
            </div>
          </div>

          <div className="card">
            <h3 style={{ marginTop: 0 }}>
              Ingredients ({matchedIngredients} / {totalIngredients} available)
            </h3>
            <div className="recipe-ingredients">
              <div>
                <div className="recipe-ingredients-title">You have</div>
                {haveList.length === 0 ? (
                  <div className="recipe-ingredients-empty">No ingredients yet.</div>
                ) : (
                  <ul>
                    {haveList.map((item) => (
                      <li key={item.ingredientId}>
                        {item.ingredientName}: {item.requiredQuantity} {item.measurementUnit} (you have {item.availableQuantity})
                      </li>
                    ))}
                  </ul>
                )}
              </div>
              <div>
                <div className="recipe-ingredients-title">Missing</div>
                {missingList.length === 0 ? (
                  <div className="recipe-ingredients-empty">Nothing missing.</div>
                ) : (
                  <ul>
                    {missingList.map((item) => (
                      <li key={item.ingredientId}>
                        {item.ingredientName}: need {item.requiredQuantity} {item.measurementUnit}, missing {item.missingQuantity}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>

          <div style={{ display: "flex", gap: 12 }}>
            <button type="button" className="secondary-btn" onClick={() => navigate(-1)}>
              Back
            </button>
            <button type="button" className="primary-btn" onClick={handleCook}>
              Cook recipe
            </button>
          </div>
        </div>

        {/* Coloana Dreaptă: Instrucțiuni de preparare */}
        <div className="grid" style={{ gap: 16 }}>
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Preparation Instructions</h3>
            {recipe.description ? (
              <p style={{ margin: 0, whiteSpace: "pre-wrap", lineHeight: "1.6" }}>
                {recipe.description}
              </p>
            ) : (
              <p style={{ margin: 0, color: "#6b7280" }}>
                No specific instructions provided for this recipe.
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}
