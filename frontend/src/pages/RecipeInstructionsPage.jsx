import { useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { getRecipeById } from "../api/recipes.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function RecipeInstructionsPage() {
  const { recipeId } = useParams();
  const { token } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [status, setStatus] = useState(location.state?.status || { type: "", message: "" });

  useEffect(() => {
    getRecipeById(token, recipeId)
      .then(setData)
      .catch((err) => setStatus({ type: "error", message: err.message }))
      .finally(() => setLoading(false));
  }, [recipeId, token]);

  if (loading) return <div className="card">Loading instructions...</div>;
  if (!data) return null;

  const { recipe } = data;
  const normalizedSteps = (recipe.preparationSteps || "")
    .replace(/\\r\\n/g, "\n")
    .replace(/\\n/g, "\n")
    .replace(/\/n/g, "\n");
  const stepParts = normalizedSteps.split(/\n\s*\n/);
  const instructionText = stepParts[0] || "";
  const notesText = stepParts.slice(1).join("\n\n");
  const steps = instructionText
    .split(/(?<=\.)\s+/)
    .map((step) => step.trim())
    .filter(Boolean);

  return (
    <div className="instructions-shell">
      <div className="instructions-header">
        <span className="instructions-eyebrow">Preparation guide</span>
        <h2>{recipe.name}</h2>
        <p className="instructions-subtitle">Follow each step, then come back to your ingredients whenever you need.</p>
        <button type="button" className="secondary-btn" onClick={() => navigate(`/recipe/${recipeId}`, { replace: true })}>
          Back to ingredients
        </button>
      </div>

      {status.message ? (
        <div className={status.type === "success" ? "success" : "alert"} style={{ marginBottom: 16 }}>
          {status.message}
        </div>
      ) : null}

      <div className="instructions-card">
        {steps.length > 0 ? (
          <ol className="instructions-steps">
            {steps.map((step, index) => (
              <li key={`${index}-${step}`}>
                <span className="instructions-step-number">{index + 1}</span>
                <p>{step}</p>
              </li>
            ))}
          </ol>
        ) : (
          <p className="instructions-empty">No specific instructions provided for this recipe.</p>
        )}

        {notesText ? (
          <div className="instructions-notes">
            <div className="instructions-notes-title">Prep/Cook notes</div>
            <p>{notesText.replace(/^Prep\/Cook notes:\s*/i, "")}</p>
          </div>
        ) : null}
      </div>
    </div>
  );
}
