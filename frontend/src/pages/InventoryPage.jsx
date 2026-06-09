import { useEffect, useMemo, useState } from "react";
import { addIngredient, getInventory, removeIngredient, updateIngredient } from "../api/inventory.js";
import { getIngredients } from "../api/ingredients.js";
import { useAuth } from "../context/AuthContext.jsx";

export default function InventoryPage() {
  const { token } = useAuth();
  const [status, setStatus] = useState({ type: "", message: "" });
  const [inventory, setInventory] = useState([]);
  const [ingredients, setIngredients] = useState([]);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [form, setForm] = useState({ ingredientId: "", quantity: "" });
  const [editValues, setEditValues] = useState({});
  const [focusedId, setFocusedId] = useState(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [inventorySearch, setInventorySearch] = useState("");

  const filteredIngredients = useMemo(() => {
    return ingredients.filter((ing) =>
      ing.name.toLowerCase().includes(searchQuery.toLowerCase())
    );
  }, [ingredients, searchQuery]);

  const selectedIngredient = useMemo(() => {
    const id = Number(form.ingredientId);
    return ingredients.find((item) => item.ingredientId === id) || null;
  }, [ingredients, form.ingredientId]);

  const filteredInventory = useMemo(() => {
    return inventory.filter((item) =>
      item.ingredientName.toLowerCase().includes(inventorySearch.toLowerCase())
    );
  }, [inventory, inventorySearch]);

  const loadIngredients = async () => {
    try {
      const response = await getIngredients(token);
      setIngredients(response || []);
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    }
  };

  const loadInventory = async () => {
    setLoading(true);
    setStatus({ type: "", message: "" });
    try {
      const response = await getInventory(token);
      const list = response || [];
      setInventory(list);
      setEditValues(
        list.reduce((acc, item) => {
          acc[item.ingredientId] = String(item.quantity);
          return acc;
        }, {})
      );
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!token) {
      return;
    }
    loadIngredients();
    loadInventory();
  }, [token]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!event.target.closest(".inventory-card") && !event.target.closest(".primary-btn")) {
        setFocusedId(null);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleAddSubmit = async (event) => {
    event.preventDefault();
    setStatus({ type: "", message: "" });

    const ingredientId = Number(form.ingredientId);
    const quantity = Number(form.quantity);
    if (!ingredientId) {
      setStatus({ type: "error", message: "Select an ingredient." });
      return;
    }
    if (!Number.isFinite(quantity) || quantity <= 0) {
      setStatus({ type: "error", message: "Quantity must be a positive number." });
      return;
    }

    try {
      await addIngredient(token, { ingredientId, quantity });
      setStatus({ type: "success", message: "Ingredient added." });
      setIsModalOpen(false);
      setForm({ ingredientId: "", quantity: "" });
      await loadInventory();
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    }
  };

  const handleUpdate = async (ingredientId) => {
    setStatus({ type: "", message: "" });
    const newQuantity = Number(editValues[ingredientId]);
    if (!Number.isFinite(newQuantity) || newQuantity < 0) {
      setStatus({ type: "error", message: "Quantity must be 0 or higher." });
      return;
    }

    try {
      await updateIngredient(token, { ingredientId, newQuantity });
      setStatus({ type: "success", message: "Quantity updated." });
      await loadInventory();
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    }
  };

  const handleRemove = async (ingredientId) => {
    const confirmRemove = window.confirm("Are you sure you want to remove this ingredient from your inventory?");
    if (!confirmRemove) return;

    setStatus({ type: "", message: "" });
    try {
      await removeIngredient(token, { ingredientId });
      setStatus({ type: "success", message: "Ingredient removed." });
      await loadInventory();
    } catch (error) {
      setStatus({ type: "error", message: error.message });
    }
  };

  return (
    <div className="grid" style={{ gap: 20 }}>
      <div className="section-header">
        <h2>Inventory</h2>
        <div className="inventory-toolbar">
          <input
            type="text"
            className="search-input"
            placeholder="Search in inventory..."
            value={inventorySearch}
            onChange={(e) => setInventorySearch(e.target.value)}
          />
          <button
            type="button"
            className="primary-btn"
            onClick={() => {
              setIsModalOpen(true);
              setSearchQuery("");
            }}
          >
            + Add
          </button>
        </div>
      </div>

      {status.message ? (
        <div className={status.type === "success" ? "success" : "alert"}>{status.message}</div>
      ) : null}

      {loading ? <div className="card">Loading inventory...</div> : null}

      {!loading && inventory.length === 0 ? (
        <div className="card">No items in inventory yet.</div>
      ) : null}

      <div className="grid inventory-grid">
        {filteredInventory.map((item) => (
          <div
            className={`card inventory-card ${focusedId === item.ingredientId ? "focused" : ""}`}
            key={item.ingredientId}
            onClick={() => setFocusedId(item.ingredientId)}
          >
            <div className="inventory-title">{item.ingredientName}</div>
            <div className="badge">Unit: {item.measurementUnit}</div>
            <div className="form-row">
              <input
                type="number"
                min="0"
                step="1"
                value={editValues[item.ingredientId] ?? ""}
                onFocus={() => setFocusedId(item.ingredientId)}
                onChange={(event) =>
                  setEditValues((prev) => ({
                    ...prev,
                    [item.ingredientId]: event.target.value
                  }))
                }
              />
              {focusedId === item.ingredientId && (
                <div className="inventory-actions">
                  <button
                    type="button"
                    className="secondary-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleUpdate(item.ingredientId);
                    }}
                  >
                    Save
                  </button>
                  <button
                    type="button"
                    className="danger-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleRemove(item.ingredientId);
                    }}
                  >
                    Remove
                  </button>
                </div>
              )}
            </div>
          </div>
        ))}
      </div>

      {isModalOpen ? (
        <div
          className="modal-backdrop"
          onClick={() => {
            setIsModalOpen(false);
            setSearchQuery("");
          }}
        >
          <div className="modal inventory-modal" onClick={(event) => event.stopPropagation()}>
            <h3>Add ingredient</h3>
            <form className="form-row" onSubmit={handleAddSubmit}>
              <div style={{ display: "flex", flexDirection: "column", gap: 4 }}>
                <label style={{ fontSize: "12px", fontWeight: "600", color: "#6b7280" }}>Search:</label>
                <input
                  type="text"
                  className="search-input"
                  placeholder="Type to filter..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  autoFocus
                />
              </div>
              {/* Card picker */}
              <div className="planner-form-field">
                <label style={{ fontSize: "12px", fontWeight: "600", color: "#6b7280", textTransform: "uppercase", letterSpacing: "0.04em" }}>
                  Select ingredient
                </label>
                <div className="planner-recipe-picker">
                  {filteredIngredients.length === 0 && (
                    <div className="planner-recipe-picker-empty">No ingredients found.</div>
                  )}
                  {filteredIngredients.map((ing) => {
                    const isSelected = form.ingredientId === String(ing.ingredientId);
                    return (
                      <button
                        key={ing.ingredientId}
                        type="button"
                        className={`planner-recipe-card ${isSelected ? "selected" : ""}`}
                        onClick={() => setForm((prev) => ({ ...prev, ingredientId: String(ing.ingredientId) }))}
                      >
                        <span className="planner-recipe-card-name">{ing.name}</span>
                        <span className="planner-recipe-card-meta">Unit: {ing.measurementUnit}</span>
                      </button>
                    );
                  })}
                </div>
              </div>
              <input
                type="number"
                min="1"
                step="1"
                placeholder="Quantity"
                value={form.quantity}
                onChange={(event) => setForm((prev) => ({ ...prev, quantity: event.target.value }))}
                required
              />
              <div className="badge">Unit: {selectedIngredient?.measurementUnit || "-"}</div>
              <div className="inventory-actions">
                <button type="submit" className="primary-btn">
                  Add
                </button>
                <button type="button" className="secondary-btn" onClick={() => setIsModalOpen(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </div>
  );
}
