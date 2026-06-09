import { DIETS } from "../data/diets.js";

export default function DietFilter({ value, onChange }) {
  const selectedDiets = Array.isArray(value) ? value : value ? [value] : [];

  const toggleDiet = (dietName) => {
    if (selectedDiets.includes(dietName)) {
      onChange(selectedDiets.filter((name) => name !== dietName));
      return;
    }
    onChange([...selectedDiets, dietName]);
  };

  return (
    <div className="card">
      <h3>Diet filter</h3>
      <div className="diet-filter-list" role="group" aria-label="Diet filters">
        {DIETS.map((diet) => (
          <button
            key={diet.id}
            type="button"
            className={`diet-filter-toggle ${selectedDiets.includes(diet.name) ? "selected" : ""}`}
            aria-pressed={selectedDiets.includes(diet.name)}
            onClick={() => toggleDiet(diet.name)}
          >
            {diet.name}
          </button>
        ))}
      </div>
      {selectedDiets.length > 0 ? (
        <button type="button" className="secondary-btn diet-clear-btn" onClick={() => onChange([])}>
          Clear filters
        </button>
      ) : null}
    </div>
  );
}

