export default function RecipeCard({ recipe, match, details, onCook }) {
  if (!recipe) {
    return null;
  }

  const dietNames = recipe.dietClassifications?.map((diet) => diet.name).join(", ");
  const ingredients = details?.ingredients || [];
  const haveList = ingredients.filter((item) => item.missingQuantity === 0);
  const missingList = ingredients.filter((item) => item.missingQuantity > 0);

  return (
    <div className="card">
      <div className="section-header">
        <h3>{recipe.name}</h3>
        {match ? (
          <span className="badge">{match.matchPercentage}% match</span>
        ) : null}
      </div>
      <div className="form-row">
        {recipe.caloriesKcal ? <div>Calories: {recipe.caloriesKcal}</div> : null}
        {match ? (
          <div>
            Matched {match.matchedIngredients} / {match.totalIngredients}
          </div>
        ) : null}
        {dietNames ? <div>Diets: {dietNames}</div> : null}

        {ingredients.length > 0 ? (
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
        ) : null}

        <button type="button" className="green-btn" onClick={onCook}>
          View recipe
        </button>
      </div>
    </div>
  );
}
