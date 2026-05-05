import { DIETS } from "../data/diets.js";

export default function DietFilter({ value, onChange }) {
  return (
    <div className="card">
      <h3>Diet filter</h3>
      <div className="form-row">
        <select value={value} onChange={(event) => onChange(event.target.value)}>
          <option value="">All diets</option>
          {DIETS.map((diet) => (
            <option key={diet.id} value={diet.name}>
              {diet.name}
            </option>
          ))}
        </select>

      </div>
    </div>
  );
}

