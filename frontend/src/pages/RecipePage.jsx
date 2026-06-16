import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { cookRecipe, getRecipeById } from "../api/recipes.js";
import { useAuth } from "../context/AuthContext.jsx";

const formatQty = (val) => {
  if (val === null || val === undefined) return "";
  const num = Number(val);
  if (Number.isNaN(num)) return val;
  return parseFloat(num.toFixed(2));
};

export default function RecipePage() {
  const { recipeId } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [showCookModal, setShowCookModal] = useState(false);
  const [cookLoading, setCookLoading] = useState(false);
  const [status, setStatus] = useState({ type: "", message: "" });

  useEffect(() => {
    getRecipeById(token, recipeId)
      .then(setData)
      .catch((err) => setStatus({ type: "error", message: err.message }))
      .finally(() => setLoading(false));
  }, [recipeId, token]);

  const openCookModal = () => {
    setStatus({ type: "", message: "" });
    setShowCookModal(true);
  };

  const continueWithoutDeducting = () => {
    setShowCookModal(false);
    navigate(`/recipe/${recipeId}/instructions`, {
      state: { status: { type: "success", message: "Recipe opened without changing your inventory." } }
    });
  };

  const cookAndDeductIngredients = async () => {
    setCookLoading(true);
    setStatus({ type: "", message: "" });
    try {
      await cookRecipe(token, recipeId);
      setShowCookModal(false);
      navigate(`/recipe/${recipeId}/instructions`, {
        state: { status: { type: "success", message: "Ingredients have been deducted from your inventory." } }
      });
    } catch (err) {
      setStatus({ type: "error", message: err.message });
      setShowCookModal(false);
    } finally {
      setCookLoading(false);
    }
  };

  const handleDietClick = (dietName) => {
    navigate("/", { state: { dietFilters: [dietName] } });
  };

  if (loading) return <div className="card">Loading...</div>;
  if (!data) return null;

  const { recipe, matchedIngredients, totalIngredients, matchPercentage, ingredients } = data;
  const diets = recipe.dietClassifications || [];
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

      <div className={`recipe-details-layout ${!recipe.description ? "recipe-details-layout-single" : ""}`}>
        <div className="grid" style={{ gap: 16 }}>
          <div className="card">
            <h3 style={{ marginTop: 0 }}>Nutritional Information</h3>
            <div className="form-row">
              {recipe.totalPrepTimeMinutes ? <div>Prep time: {recipe.totalPrepTimeMinutes} min</div> : null}
              {recipe.caloriesKcal ? <div>Calories: {recipe.caloriesKcal} kcal</div> : null}
              {recipe.proteinsG ? <div>Protein: {recipe.proteinsG} g</div> : null}
              {recipe.carbohydratesG ? <div>Carbs: {recipe.carbohydratesG} g</div> : null}
              {recipe.fatsG ? <div>Fats: {recipe.fatsG} g</div> : null}
              {diets.length > 0 ? (
                <div>
                  <div className="recipe-ingredients-title">Diets</div>
                  <div className="diet-tag-list">
                    {diets.map((diet) => (
                      <button
                        key={diet.dietId || diet.id || diet.name}
                        type="button"
                        className="diet-tag-btn"
                        onClick={() => handleDietClick(diet.name)}
                      >
                        {diet.name}
                      </button>
                    ))}
                  </div>
                </div>
              ) : null}
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
                        {item.ingredientName}: {formatQty(item.requiredQuantity)} {item.measurementUnit} (you have {formatQty(item.availableQuantity)})
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
                        {item.ingredientName}: need {formatQty(item.requiredQuantity)} {item.measurementUnit}, missing {formatQty(item.missingQuantity)}{item.availableQuantity > 0 ? ` (you have ${formatQty(item.availableQuantity)})` : ""}
                      </li>
                    ))}
                  </ul>
                )}
              </div>
            </div>
          </div>

          <div className="recipe-action-row">
            <button type="button" className="secondary-btn" onClick={() => navigate(-1)}>
              Back
            </button>
            <button type="button" className="primary-btn" onClick={openCookModal}>
              Cook recipe
            </button>
          </div>
        </div>

        {recipe.description ? (
          <div className="card">
            <h3 style={{ marginTop: 0 }}>About this recipe</h3>
            <p style={{ margin: 0, color: "#4b5563", lineHeight: 1.6 }}>{recipe.description}</p>
          </div>
        ) : null}
      </div>

      {showCookModal ? (
        <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="cook-modal-title">
          <div className="modal cook-confirm-modal">
            <div className="cook-confirm-icon">Cook</div>
            <h3 id="cook-modal-title">Remove ingredients from inventory?</h3>
            <p>
              You can deduct the required ingredients now, or keep your inventory unchanged and continue to the
              preparation instructions.
            </p>
            <div className="cook-confirm-actions">
              <button
                type="button"
                className="secondary-btn"
                onClick={continueWithoutDeducting}
                disabled={cookLoading}
              >
                No, keep inventory
              </button>
              <button
                type="button"
                className="primary-btn"
                onClick={cookAndDeductIngredients}
                disabled={cookLoading}
              >
                {cookLoading ? "Cooking..." : "Yes, remove items"}
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </div>
  );
}
