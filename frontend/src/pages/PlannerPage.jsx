import { useEffect, useMemo, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { getRecipeDetails } from "../api/recipes.js";
import { addIngredient, updateIngredient } from "../api/inventory.js";
import {
  getPlans,
  createPlan,
  updatePlan,
  deletePlan,
  getPlanDetails,
  addPlanDetail,
  updatePlanDetail,
  deletePlanDetail,
  generateAiPlan,
} from "../api/plans.js";
import { useNavigate } from "react-router-dom";

const MEAL_TYPES = ["breakfast", "lunch", "dinner", "snack"];
const MEAL_ICONS = { breakfast: "🌅", lunch: "☀️", dinner: "🌙", snack: "🍎" };
const MAX_PLAN_DAYS = 31;

function formatDate(iso) {
  if (!iso) return "";
  return new Date(iso).toLocaleDateString("en-GB", {
    day: "2-digit", month: "short", year: "numeric",
  });
}
function today() { return new Date().toISOString().slice(0, 10); }
function addDays(dateStr, n) {
  const d = new Date(dateStr);
  d.setDate(d.getDate() + n);
  return d.toISOString().slice(0, 10);
}
function daysBetween(start, end) {
  return Math.round((new Date(end) - new Date(start)) / 86400000) + 1;
}
function dateRange(startDate, endDate) {
  const days = [];
  let cur = new Date(startDate);
  const end = new Date(endDate);
  while (cur <= end) { days.push(cur.toISOString().slice(0, 10)); cur.setDate(cur.getDate() + 1); }
  return days;
}
function dayLabel(dateStr) {
  return new Date(dateStr).toLocaleDateString("en-GB", { weekday: "short", day: "2-digit", month: "short" });
}
const formatQty = (val) => {
  if (val === null || val === undefined) return "";
  const num = Number(val);
  if (Number.isNaN(num)) return val;
  return parseFloat(num.toFixed(2));
};

// ─────────────────────────────────────────────────────────────────────────────

export default function PlannerPage() {
  const { token } = useAuth();
  const navigate = useNavigate();

  const [plans, setPlans] = useState([]);
  const [activePlanId, setActivePlanId] = useState(null);
  const [planDetails, setPlanDetails] = useState([]);
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ type: "", message: "" });

  // Plan modal
  const [showPlanModal, setShowPlanModal] = useState(false);
  const [editingPlan, setEditingPlan] = useState(null);
  const [planForm, setPlanForm] = useState({ name: "", startDate: today(), endDate: addDays(today(), 6) });
  const [planDayError, setPlanDayError] = useState("");
  const [modalStep, setModalStep] = useState(1); // 1 = dates, 2 = AI prompt
  const [aiPrompt, setAiPrompt] = useState("");
  const [aiGenerating, setAiGenerating] = useState(false);

  // Add-slot modal
  const [showSlotModal, setShowSlotModal] = useState(false);
  const [slotForm, setSlotForm] = useState({ dayNumber: 1, mealType: "breakfast", recipeId: null, quantity: 1 });
  const [recipeSearch, setRecipeSearch] = useState("");

  // Day-details modal (ingredient table)
  const [dayDetailsModal, setDayDetailsModal] = useState(null); // { dayNum, dateStr }
  const [addingInventory, setAddingInventory] = useState(false);

  // ── Data loading ────────────────────────────────────────────────────────────

  const loadPlans = async () => {
    setLoading(true);
    try {
      const data = await getPlans(token);
      setPlans(data || []);
      if ((data || []).length > 0 && !activePlanId) setActivePlanId(data[0].planId);
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    } finally {
      setLoading(false);
    }
  };

  const loadPlanDetails = async (planId) => {
    try {
      const data = await getPlanDetails(token, planId);
      setPlanDetails(data || []);
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  const loadRecipes = async () => {
    try {
      const data = await getRecipeDetails(token);
      setRecipes(data || []);
    } catch (_) {}
  };

  useEffect(() => { if (!token) return; loadPlans(); loadRecipes(); }, [token]);
  useEffect(() => { if (!activePlanId) { setPlanDetails([]); return; } loadPlanDetails(activePlanId); }, [activePlanId]);

  // ── Derived state ───────────────────────────────────────────────────────────

  const activePlan = useMemo(() => plans.find((p) => p.planId === activePlanId) || null, [plans, activePlanId]);
  const calendarDays = useMemo(() => activePlan ? dateRange(activePlan.startDate, activePlan.endDate) : [], [activePlan]);

  // details grouped by "dayNumber-mealType"
  const grouped = useMemo(() => {
    const map = {};
    for (const d of planDetails) {
      const key = `${d.dayNumber}-${d.mealType}`;
      if (!map[key]) map[key] = [];
      map[key].push(d);
    }
    return map;
  }, [planDetails]);

  // recipeId -> ingredient list  { ingredientId, ingredientName, measurementUnit, requiredQuantity }
  const recipeIngredientMap = useMemo(() => {
    const map = {};
    for (const r of recipes) {
      if (r.recipe?.recipeId) map[r.recipe.recipeId] = r.ingredients || [];
    }
    return map;
  }, [recipes]);

  // Filtered recipes for slot modal
  const filteredRecipes = useMemo(() => {
    const q = recipeSearch.trim().toLowerCase();
    return recipes.filter((r) => !q || (r.recipe?.name || "").toLowerCase().includes(q));
  }, [recipes, recipeSearch]);

  // Build ingredient table for a given day
  // Each row: { ingredientId, name, unit, totalQty, availableQty }
  const buildDayIngredients = (dayNum) => {
    const dayMeals = planDetails.filter((d) => d.dayNumber === dayNum);
    const agg = {};
    for (const meal of dayMeals) {
      const servings = meal.quantity || 1;
      const ings = recipeIngredientMap[meal.recipeId] || [];
      for (const ing of ings) {
        const key = ing.ingredientId ?? ing.ingredientName;
        if (!agg[key]) {
          agg[key] = {
            ingredientId: ing.ingredientId,
            name: ing.ingredientName,
            unit: ing.measurementUnit,
            totalQty: 0,
            availableQty: ing.availableQuantity ?? 0,
          };
        }
        agg[key].totalQty += (ing.requiredQuantity || 0) * servings;
        // availableQty is inventory-level (same value regardless of recipe), keep first
      }
    }
    return Object.values(agg).sort((a, b) => a.name.localeCompare(b.name));
  };

  // ── Plan date validation ─────────────────────────────────────────────────────

  const validatePlanDates = (start, end) => {
    if (!start || !end) return "";
    const days = daysBetween(start, end);
    if (days < 1) return "End date must be after start date.";
    if (days > MAX_PLAN_DAYS) return `Plans can be at most ${MAX_PLAN_DAYS} days.`;
    return "";
  };

  const handlePlanDateChange = (field, value) => {
    const updated = { ...planForm, [field]: value };
    setPlanForm(updated);
    setPlanDayError(validatePlanDates(updated.startDate, updated.endDate));
  };

  // ── Plan CRUD ───────────────────────────────────────────────────────────────

  const openCreatePlan = () => {
    setEditingPlan(null);
    const start = today();
    setPlanForm({ name: "", startDate: start, endDate: addDays(start, 6) });
    setPlanDayError("");
    setModalStep(1);
    setAiPrompt("");
    setShowPlanModal(true);
  };

  const openEditPlan = (plan) => {
    setEditingPlan(plan);
    setPlanForm({ name: plan.name, startDate: plan.startDate, endDate: plan.endDate });
    setPlanDayError("");
    setModalStep(1); // edit goes straight to step 1 only
    setAiPrompt("");
    setShowPlanModal(true);
  };

  // Step 1 submit: for new plans, advance to AI step; for edits, save directly
  const handlePlanStep1 = (e) => {
    e.preventDefault();
    const err = validatePlanDates(planForm.startDate, planForm.endDate);
    if (err) { setPlanDayError(err); return; }
    if (editingPlan) {
      handlePlanSubmit();
    } else {
      setModalStep(2);
    }
  };

  const handlePlanSubmit = async (skipAi = false) => {
    setStatus({ type: "", message: "" });
    try {
      if (editingPlan) {
        await updatePlan(token, editingPlan.planId, planForm);
        setStatus({ type: "success", message: "Plan updated." });
        setShowPlanModal(false);
        await loadPlans();
        await loadPlanDetails(editingPlan.planId);
      } else {
        // Create the blank plan first
        const created = await createPlan(token, planForm);
        setActivePlanId(created.planId);
        setShowPlanModal(false);
        await loadPlans();
        // Now generate meals if the user gave a prompt
        if (!skipAi && aiPrompt.trim()) {
          setAiGenerating(true);
          setStatus({ type: "", message: "" });
          try {
            const numDays = daysBetween(planForm.startDate, planForm.endDate);
            await generateAiPlan(token, { planId: created.planId, prompt: aiPrompt.trim(), numDays });
            setStatus({ type: "success", message: "✨ AI meal plan generated!" });
          } catch (aiErr) {
            setStatus({ type: "error", message: "Plan created, but AI generation failed: " + aiErr.message });
          } finally {
            setAiGenerating(false);
          }
          await loadPlanDetails(created.planId);
        } else {
          setStatus({ type: "success", message: "Plan created!" });
        }
      }
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  const handleDeletePlan = async (planId) => {
    if (!window.confirm("Delete this plan and all its meals?")) return;
    try {
      await deletePlan(token, planId);
      const remaining = plans.filter((p) => p.planId !== planId);
      setPlans(remaining);
      setActivePlanId(remaining.length > 0 ? remaining[0].planId : null);
      setStatus({ type: "success", message: "Plan deleted." });
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  // ── Add missing ingredients to inventory ─────────────────────────────────

  const handleAddMissingToInventory = async (ingRows) => {
    const missing = ingRows.filter((r) => r.ingredientId && r.totalQty > r.availableQty);
    if (missing.length === 0) return;
    setAddingInventory(true);
    const errors = [];
    for (const row of missing) {
      const deficit = formatQty(row.totalQty - row.availableQty);
      try {
        if (row.availableQty === 0) {
          // Not in inventory yet — add it
          await addIngredient(token, { ingredientId: row.ingredientId, quantity: deficit });
        } else {
          // Already in inventory — top it up to the required total
          await updateIngredient(token, { ingredientId: row.ingredientId, newQuantity: formatQty(row.availableQty + deficit) });
        }
      } catch (e) {
        // If add failed because already exists, try update instead
        try {
          await updateIngredient(token, { ingredientId: row.ingredientId, newQuantity: formatQty(row.availableQty + deficit) });
        } catch {
          errors.push(row.name);
        }
      }
    }
    setAddingInventory(false);
    if (errors.length > 0) {
      setStatus({ type: "error", message: `Could not add: ${errors.join(", ")}` });
    } else {
      setStatus({ type: "success", message: `Added ${missing.length} missing ingredient${missing.length !== 1 ? "s" : ""} to inventory.` });
      // Reload recipes to refresh availableQty
      await loadRecipes();
      setDayDetailsModal(null);
    }
  };

  // ── Slot CRUD ───────────────────────────────────────────────────────────────

  const openAddSlot = (dayNumber, mealType) => {
    setSlotForm({ dayNumber, mealType, recipeId: null, quantity: 1 });
    setRecipeSearch("");
    setShowSlotModal(true);
  };

  const handleSlotSubmit = async (e) => {
    e.preventDefault();
    if (!slotForm.recipeId) { setStatus({ type: "error", message: "Please select a recipe." }); return; }
    setStatus({ type: "", message: "" });
    try {
      await addPlanDetail(token, activePlanId, {
        recipeId: Number(slotForm.recipeId),
        mealType: slotForm.mealType,
        dayNumber: slotForm.dayNumber,
        quantity: Number(slotForm.quantity),
      });
      setShowSlotModal(false);
      await loadPlanDetails(activePlanId);
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  const handleToggleFollowed = async (detail) => {
    try {
      await updatePlanDetail(token, detail.planDetailId, !detail.isFollowed);
      setPlanDetails((prev) =>
        prev.map((d) => d.planDetailId === detail.planDetailId ? { ...d, isFollowed: !d.isFollowed } : d)
      );
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  // Mark all meals in a day as followed/unfollowed
  const handleMarkAllDay = async (dayNum) => {
    const dayMeals = planDetails.filter((d) => d.dayNumber === dayNum);
    if (dayMeals.length === 0) return;
    const allFollowed = dayMeals.every((d) => d.isFollowed);
    const newState = !allFollowed;
    try {
      await Promise.all(dayMeals.map((d) => updatePlanDetail(token, d.planDetailId, newState)));
      setPlanDetails((prev) =>
        prev.map((d) => d.dayNumber === dayNum ? { ...d, isFollowed: newState } : d)
      );
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  const handleDeleteDetail = async (detailId) => {
    try {
      await deletePlanDetail(token, detailId);
      setPlanDetails((prev) => prev.filter((d) => d.planDetailId !== detailId));
    } catch (e) {
      setStatus({ type: "error", message: e.message });
    }
  };

  // ── Progress ────────────────────────────────────────────────────────────────

  const followedCount = planDetails.filter((d) => d.isFollowed).length;
  const totalCount = planDetails.length;
  const progressPct = totalCount === 0 ? 0 : Math.round((followedCount / totalCount) * 100);

  // ── Render ──────────────────────────────────────────────────────────────────

  return (
    <div className="planner-shell">

      {/* ── Sidebar ─────────────────────────────────────────────────────────── */}
      <aside className="planner-sidebar">
        <div className="planner-sidebar-header">
          <h2>📅 My Plans</h2>
          <button className="primary-btn planner-new-btn" onClick={openCreatePlan}>+ New</button>
        </div>

        {loading && <div className="planner-sidebar-empty">Loading…</div>}
        {!loading && plans.length === 0 && (
          <div className="planner-sidebar-empty">No plans yet. Create your first one!</div>
        )}

        <ul className="planner-plan-list">
          {plans.map((plan) => (
            <li
              key={plan.planId}
              className={`planner-plan-item ${plan.planId === activePlanId ? "active" : ""}`}
              onClick={() => setActivePlanId(plan.planId)}
            >
              <div className="planner-plan-item-name">{plan.name}</div>
              <div className="planner-plan-item-dates">{formatDate(plan.startDate)} → {formatDate(plan.endDate)}</div>
              <div className="planner-plan-item-actions">
                <button className="planner-icon-btn edit" title="Edit plan"
                  onClick={(e) => { e.stopPropagation(); openEditPlan(plan); }}>✏️</button>
                <button className="planner-icon-btn danger" title="Delete plan"
                  onClick={(e) => { e.stopPropagation(); handleDeletePlan(plan.planId); }}>🗑️</button>
              </div>
            </li>
          ))}
        </ul>
      </aside>

      {/* ── Main ────────────────────────────────────────────────────────────── */}
      <main className="planner-main">
        {status.message && (
          <div className={`${status.type === "success" ? "success" : "alert"} planner-status`}>
            {status.message}
          </div>
        )}

        {aiGenerating && (
          <div className="planner-ai-generating">
            <span className="planner-ai-spinner" />
            <span>✨ AI is building your meal plan… this may take a moment</span>
          </div>
        )}

        {!activePlan ? (
          <div className="planner-empty-state">
            <div className="planner-empty-icon">📋</div>
            <h3>No plan selected</h3>
            <p>Select a plan from the sidebar or create a new one to get started.</p>
            <button className="primary-btn" onClick={openCreatePlan}>+ Create your first plan</button>
          </div>
        ) : (
          <>
            {/* Plan header */}
            <div className="planner-plan-header">
              <div>
                <h2 className="planner-plan-title">{activePlan.name}</h2>
                <span className="planner-plan-dates">
                  {formatDate(activePlan.startDate)} → {formatDate(activePlan.endDate)}
                  &nbsp;·&nbsp;{calendarDays.length} day{calendarDays.length !== 1 ? "s" : ""}
                </span>
              </div>
              <div className="planner-progress-wrap">
                <div className="planner-progress-label">
                  {followedCount}/{totalCount} meals followed&nbsp;<strong>{progressPct}%</strong>
                </div>
                <div className="planner-progress-bar">
                  <div className="planner-progress-fill" style={{ width: `${progressPct}%` }} />
                </div>
              </div>
            </div>

            {/* Calendar */}
            <div className="planner-calendar">
              {calendarDays.map((dateStr, idx) => {
                const dayNum = idx + 1;
                const dayMeals = planDetails.filter((d) => d.dayNumber === dayNum);
                const allFollowed = dayMeals.length > 0 && dayMeals.every((d) => d.isFollowed);
                const ingRows = buildDayIngredients(dayNum);

                return (
                  <div key={dateStr} className="planner-day-col">
                    {/* Day header */}
                    <div className="planner-day-label">{dayLabel(dateStr)}</div>

                    {/* Meal slots */}
                    {MEAL_TYPES.map((meal) => {
                      const key = `${dayNum}-${meal}`;
                      const items = grouped[key] || [];
                      const slotFull = items.length > 0; // one meal per slot max

                      return (
                        <div key={meal} className="planner-meal-slot">
                          <div className="planner-meal-slot-header">
                            <span>{MEAL_ICONS[meal]} {meal.charAt(0).toUpperCase() + meal.slice(1)}</span>
                            {/* Hide + button when slot is already filled */}
                            {!slotFull && (
                              <button
                                className="planner-add-meal-btn"
                                title={`Add ${meal}`}
                                onClick={() => openAddSlot(dayNum, meal)}
                              >+</button>
                            )}
                          </div>
                          {items.length === 0 ? (
                            <div className="planner-meal-empty">—</div>
                          ) : (
                            <ul className="planner-meal-list">
                              {items.map((detail) => (
                                <li
                                  key={detail.planDetailId}
                                  className={`planner-meal-entry ${detail.isFollowed ? "followed" : ""}`}
                                >
                                  <button
                                    className="planner-check-btn"
                                    onClick={() => handleToggleFollowed(detail)}
                                    title={detail.isFollowed ? "Mark as not followed" : "Mark as followed"}
                                  >{detail.isFollowed ? "✅" : "⬜"}</button>
                                  <span
                                    className="planner-meal-name"
                                    onClick={() => navigate(`/recipe/${detail.recipeId}`)}
                                    title="View recipe"
                                  >
                                    {detail.recipeName}
                                    {detail.quantity > 1 && (
                                      <span className="planner-qty-badge">×{detail.quantity}</span>
                                    )}
                                  </span>
                                  <button
                                    className="planner-del-btn"
                                    onClick={() => handleDeleteDetail(detail.planDetailId)}
                                    title="Remove"
                                  >×</button>
                                </li>
                              ))}
                            </ul>
                          )}
                        </div>
                      );
                    })}

                    {/* Day footer: mark-all + details button */}
                    {dayMeals.length > 0 && (
                      <div className="planner-day-footer">
                        <button
                          className="planner-footer-btn mark-all"
                          onClick={() => handleMarkAllDay(dayNum)}
                          title={allFollowed ? "Unmark all" : "Mark all as followed"}
                        >
                          {allFollowed ? "✅ Unmark all" : "☑️ Mark all"}
                        </button>
                        <button
                          className="planner-footer-btn details"
                          onClick={() => setDayDetailsModal({ dayNum, dateStr })}
                          title="View day ingredients"
                          disabled={ingRows.length === 0}
                        >
                          🛒 Ingredients
                        </button>
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </>
        )}
      </main>

      {/* ── Plan create/edit modal ───────────────────────────────────────────── */}
      {showPlanModal && (
        <div className="modal-backdrop" onClick={() => setShowPlanModal(false)}>
          <div className="modal planner-modal" onClick={(e) => e.stopPropagation()}>

            {/* ── Step 1: Name + Dates ── */}
            {modalStep === 1 && (
              <>
                <h3>{editingPlan ? "Edit Plan" : "Create New Plan"}</h3>
                <form className="form-row" onSubmit={handlePlanStep1}>
                  <div className="planner-form-field">
                    <label>Plan name</label>
                    <input
                      type="text"
                      placeholder="e.g. Week 1 Healthy Eating"
                      value={planForm.name}
                      onChange={(e) => setPlanForm((p) => ({ ...p, name: e.target.value }))}
                      required autoFocus
                    />
                  </div>
                  <div className="planner-form-row-2">
                    <div className="planner-form-field">
                      <label>Start date</label>
                      <input type="date" value={planForm.startDate}
                        onChange={(e) => handlePlanDateChange("startDate", e.target.value)} required />
                    </div>
                    <div className="planner-form-field">
                      <label>End date</label>
                      <input type="date" value={planForm.endDate}
                        min={planForm.startDate}
                        max={addDays(planForm.startDate, MAX_PLAN_DAYS - 1)}
                        onChange={(e) => handlePlanDateChange("endDate", e.target.value)} required />
                    </div>
                  </div>
                  {planDayError && (
                    <div className="alert" style={{ padding: "8px 12px", fontSize: 13 }}>{planDayError}</div>
                  )}
                  {planForm.startDate && planForm.endDate && !planDayError && (
                    <div style={{ fontSize: 13, color: "#6b7280" }}>
                      📅 {daysBetween(planForm.startDate, planForm.endDate)} day{daysBetween(planForm.startDate, planForm.endDate) !== 1 ? "s" : ""} (max {MAX_PLAN_DAYS})
                    </div>
                  )}
                  {editingPlan && (
                    <div style={{ fontSize: 12, color: "#b45309", background: "#fef3c7", padding: "8px 12px", borderRadius: 6 }}>
                      ⚠️ Shortening the plan will permanently delete meals beyond the new end date.
                    </div>
                  )}
                  <div className="inventory-actions">
                    <button type="submit" className="primary-btn" disabled={!!planDayError}>
                      {editingPlan ? "Save changes" : "Next →"}
                    </button>
                    <button type="button" className="secondary-btn" onClick={() => setShowPlanModal(false)}>Cancel</button>
                  </div>
                </form>
              </>
            )}

            {/* ── Step 2: AI Prompt (create only) ── */}
            {modalStep === 2 && (
              <>
                <div className="planner-ai-step-header">
                  <button className="planner-back-btn" onClick={() => setModalStep(1)}>← Back</button>
                  <h3>✨ Generate meals with AI</h3>
                </div>
                <p className="planner-ai-step-sub">
                  Describe what you're looking for — dietary preferences, goals, cuisine style, or anything else.
                  The AI will fill your {daysBetween(planForm.startDate, planForm.endDate)}-day plan using recipes from the database.
                </p>
                <div className="planner-form-field">
                  <label>Your preferences</label>
                  <textarea
                    className="planner-ai-textarea"
                    placeholder="e.g. High protein, no dairy, Mediterranean style, easy to prepare…"
                    value={aiPrompt}
                    onChange={(e) => setAiPrompt(e.target.value)}
                    rows={4}
                    autoFocus
                  />
                </div>
                <div className="planner-ai-actions">
                  <button
                    className="primary-btn planner-ai-generate-btn"
                    onClick={() => handlePlanSubmit(false)}
                    disabled={!aiPrompt.trim()}
                  >
                    ✨ Create &amp; Generate plan
                  </button>
                  <button
                    className="secondary-btn"
                    onClick={() => handlePlanSubmit(true)}
                  >
                    Skip — create blank plan
                  </button>
                </div>
              </>
            )}

          </div>
        </div>
      )}

      {/* ── Add meal slot modal ──────────────────────────────────────────────── */}
      {showSlotModal && (
        <div className="modal-backdrop" onClick={() => setShowSlotModal(false)}>
          <div className="modal planner-modal planner-slot-modal" onClick={(e) => e.stopPropagation()}>
            <h3>
              {MEAL_ICONS[slotForm.mealType]}&nbsp;
              Add {slotForm.mealType.charAt(0).toUpperCase() + slotForm.mealType.slice(1)} — Day {slotForm.dayNumber}
            </h3>
            <form onSubmit={handleSlotSubmit} style={{ display: "flex", flexDirection: "column", gap: 16 }}>
              <div className="planner-form-field">
                <label>Search recipe</label>
                <input type="text" placeholder="Type to filter…" value={recipeSearch}
                  onChange={(e) => setRecipeSearch(e.target.value)} autoFocus />
              </div>
              <div className="planner-form-field">
                <label>Select a recipe</label>
                <div className="planner-recipe-picker">
                  {filteredRecipes.length === 0 && (
                    <div className="planner-recipe-picker-empty">No recipes found.</div>
                  )}
                  {filteredRecipes.map((r) => {
                    const id = r.recipe.recipeId;
                    const isSelected = slotForm.recipeId === id;
                    return (
                      <button key={id} type="button"
                        className={`planner-recipe-card ${isSelected ? "selected" : ""}`}
                        onClick={() => setSlotForm((p) => ({ ...p, recipeId: id }))}>
                        <span className="planner-recipe-card-name">{r.recipe.name}</span>
                        <span className="planner-recipe-card-meta">
                          {r.totalIngredients} ingredient{r.totalIngredients !== 1 ? "s" : ""}
                          {r.recipe.caloriesKcal ? ` · ${Math.round(r.recipe.caloriesKcal)} kcal` : ""}
                        </span>
                      </button>
                    );
                  })}
                </div>
              </div>
              <div className="planner-form-field">
                <label>Servings</label>
                <input type="number" min="1" max="20" value={slotForm.quantity}
                  onChange={(e) => setSlotForm((p) => ({ ...p, quantity: e.target.value }))} required />
              </div>
              <div className="inventory-actions">
                <button type="submit" className="primary-btn" disabled={!slotForm.recipeId}>Add to plan</button>
                <button type="button" className="secondary-btn" onClick={() => setShowSlotModal(false)}>Cancel</button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Day ingredient details modal ─────────────────────────────────────── */}
      {dayDetailsModal && (() => {
        const { dayNum, dateStr } = dayDetailsModal;
        const ingRows = buildDayIngredients(dayNum);
        const dayMeals = planDetails.filter((d) => d.dayNumber === dayNum);
        const missingCount = ingRows.filter((r) => r.ingredientId && r.totalQty > r.availableQty).length;

        return (
          <div className="modal-backdrop" onClick={() => setDayDetailsModal(null)}>
            <div className="modal planner-modal planner-details-modal" onClick={(e) => e.stopPropagation()}>
              <div className="planner-details-header">
                <div>
                  <h3>🛒 Day {dayNum} — {dayLabel(dateStr)}</h3>
                  <p className="planner-details-subtitle">
                    Ingredients needed across {dayMeals.length} meal{dayMeals.length !== 1 ? "s" : ""}
                    {dayMeals.some((d) => d.quantity > 1) && " (quantities scaled by servings)"}
                  </p>
                </div>
                <button className="planner-modal-close" onClick={() => setDayDetailsModal(null)}>×</button>
              </div>

              {/* Meals summary */}
              <div className="planner-details-meals">
                {dayMeals.map((d) => (
                  <span key={d.planDetailId} className="planner-details-meal-badge">
                    {MEAL_ICONS[d.mealType]} {d.recipeName}
                    {d.quantity > 1 && <span className="planner-qty-badge">×{d.quantity}</span>}
                  </span>
                ))}
              </div>

              {/* Ingredient table */}
              {ingRows.length === 0 ? (
                <p style={{ color: "#9ca3af", fontStyle: "italic", fontSize: 14 }}>
                  No ingredient data available for these recipes.
                </p>
              ) : (
                <div className="planner-ing-table-wrap">
                  <table className="planner-ing-table">
                    <thead>
                      <tr>
                        <th>Ingredient</th>
                        <th>Total needed</th>
                        <th>You have</th>
                        <th>Unit</th>
                      </tr>
                    </thead>
                    <tbody>
                      {ingRows.map((row) => {
                        const enough = row.availableQty >= row.totalQty;
                        const partial = !enough && row.availableQty > 0;
                        return (
                          <tr key={row.name}>
                            <td>{row.name}</td>
                            <td className="planner-ing-qty">{formatQty(row.totalQty)}</td>
                            <td>
                              <span className={`planner-ing-have ${enough ? "ok" : partial ? "partial" : "none"}`}>
                                {formatQty(row.availableQty)}
                              </span>
                            </td>
                            <td className="planner-ing-unit">{row.unit}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}

              <div className="planner-details-footer">
                {missingCount > 0 && (
                  <button
                    className="primary-btn"
                    onClick={() => handleAddMissingToInventory(ingRows)}
                    disabled={addingInventory}
                  >
                    {addingInventory
                      ? "Adding…"
                      : `🛒 Add ${missingCount} missing ingredient${missingCount !== 1 ? "s" : ""} to inventory`}
                  </button>
                )}
                {missingCount === 0 && ingRows.length > 0 && (
                  <span className="planner-all-ok">✅ You have everything needed!</span>
                )}
                <button className="secondary-btn" onClick={() => setDayDetailsModal(null)}>Close</button>
              </div>
            </div>
          </div>
        );
      })()}
    </div>
  );
}
