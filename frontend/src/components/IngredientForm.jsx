import { useMemo, useState } from "react";
import { findIngredientByName } from "../data/ingredients.js";

export default function IngredientForm({
  title,
  actionLabel,
  showQuantity = true,
  quantityLabel = "Quantity",
  onSubmit
}) {
  const [name, setName] = useState("");
  const [quantity, setQuantity] = useState("");
  const [error, setError] = useState("");

  const ingredient = useMemo(() => findIngredientByName(name), [name]);

  const handleSubmit = (event) => {
    event.preventDefault();
    setError("");

    if (!ingredient) {
      setError("Ingredient not found in the static list.");
      return;
    }

    const numericQuantity = showQuantity ? Number(quantity) : undefined;
    if (showQuantity && (!Number.isFinite(numericQuantity) || numericQuantity <= 0)) {
      setError("Quantity must be a positive number.");
      return;
    }

    onSubmit({ ingredient, quantity: numericQuantity });
    setName("");
    setQuantity("");
  };

  return (
    <div className="card">
      <h3>{title}</h3>
      {error ? <div className="alert">{error}</div> : null}
      <form className="form-row" onSubmit={handleSubmit}>
        <input
          type="text"
          placeholder="Ingredient name"
          value={name}
          onChange={(event) => setName(event.target.value)}
          required
        />
        {showQuantity ? (
          <input
            type="number"
            step="1"
            min="1"
            placeholder={quantityLabel}
            value={quantity}
            onChange={(event) => setQuantity(event.target.value)}
            required
          />
        ) : null}
        {ingredient ? (
          <div className="badge">Unit: {ingredient.unit}</div>
        ) : (
          <div className="badge">Unit: unknown</div>
        )}
        <button type="submit" className="primary-btn">
          {actionLabel}
        </button>
      </form>
    </div>
  );
}

