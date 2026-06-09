import { useEffect, useRef, useState } from "react";
import { useAuth } from "../context/AuthContext.jsx";
import { getInventory } from "../api/inventory.js";
import { getRecipeDetails } from "../api/recipes.js";
import { sendChatPrompt } from "../api/ai.js";
import RecipeCard from "../components/RecipeCard.jsx";
import { useNavigate } from "react-router-dom";

const AI_CHEF_CHAT_LOGS_KEY = "aiChefChatLogs";
const MAX_CHAT_LOGS = 3;
const DEFAULT_MESSAGES = [
  {
    id: "welcome",
    sender: "ai",
    text: "Hi! I'm your AI Chef Assistant. Tell me what you'd like to eat or which ingredients you'd like to use, and I’ll recommend the best matching recipes!",
    recommendedRecipes: []
  }
];

const readChatLogs = () => {
  try {
    const raw = localStorage.getItem(AI_CHEF_CHAT_LOGS_KEY);
    if (!raw) return [];

    const parsed = JSON.parse(raw);
    if (!Array.isArray(parsed)) return [];

    return parsed.filter((log) =>
      log &&
      typeof log === "object" &&
      typeof log.id === "string" &&
      Array.isArray(log.messages)
    );
  } catch (_) {
    return [];
  }
};

const writeChatLogs = (logs) => {
  try {
    localStorage.setItem(AI_CHEF_CHAT_LOGS_KEY, JSON.stringify(logs));
  } catch (_) {
    // Ignore localStorage failures (private mode/quota/etc.)
  }
};

const buildMessagesFromRecentLogs = (logs) => {
  const recentLogs = logs.slice(0, MAX_CHAT_LOGS).reverse();
  const historyMessages = recentLogs.flatMap((log) =>
    (log.messages || []).filter((msg) => msg?.id !== "welcome")
  );

  return [DEFAULT_MESSAGES[0], ...historyMessages];
};

export default function AIChefPage() {
  const { token } = useAuth();
  const navigate = useNavigate();
  const [inventory, setInventory] = useState([]);
  const [detailsMap, setDetailsMap] = useState({});
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState({ type: "", message: "" });
  const sessionIdRef = useRef(`chat-${Date.now()}`);
  const didHydrateRef = useRef(false);
  const skipNextPersistRef = useRef(true);
  
  const [messages, setMessages] = useState(DEFAULT_MESSAGES);

  const messagesEndRef = useRef(null);

  // Load user inventory and recipe details
  const loadData = async () => {
    try {
      const invData = await getInventory(token);
      setInventory(invData || []);

      const detailsData = await getRecipeDetails(token);
      const map = (detailsData || []).reduce((acc, item) => {
        acc[item.recipe.recipeId] = item;
        return acc;
      }, {});
      setDetailsMap(map);
    } catch (err) {
      setStatus({ type: "error", message: err.message });
    }
  };

  useEffect(() => {
    if (token) {
      loadData();
    }
  }, [token]);

  useEffect(() => {
    const logs = readChatLogs();
    if (logs.length > 0) {
      const latest = logs[0];
      sessionIdRef.current = latest.id;
      setMessages(buildMessagesFromRecentLogs(logs));
    }
    didHydrateRef.current = true;
  }, []);

  useEffect(() => {
    if (!didHydrateRef.current) return;
    if (skipNextPersistRef.current) {
      skipNextPersistRef.current = false;
      return;
    }

    const existingLogs = readChatLogs();
    const updatedCurrent = {
      id: sessionIdRef.current,
      updatedAt: Date.now(),
      messages
    };

    const nextLogs = [
      updatedCurrent,
      ...existingLogs.filter((log) => log.id !== sessionIdRef.current)
    ].slice(0, MAX_CHAT_LOGS);

    writeChatLogs(nextLogs);
  }, [messages]);

  // Scroll to bottom when messages list changes
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages, loading]);

  const normalizeAiResponse = (aiResponse) => {
    const fallback = {
      agent1Response: "Sorry, I ran into a problem while generating recommendations.",
      recommendedRecipes: []
    };

    if (!aiResponse || typeof aiResponse !== "object") {
      return fallback;
    }

    const agent1 = aiResponse.agent1Response || "";
    if (agent1.trim().startsWith("{") && agent1.trim().endsWith("}")) {
      try {
        const parsed = JSON.parse(agent1.trim());
        return {
          agent1Response: parsed.agent1Response || fallback.agent1Response,
          recommendedRecipes: parsed.recommendedRecipeIds || parsed.recommendedRecipes || []
        };
      } catch (_) {
        // Keep the top-level payload when nested parsing fails.
      }
    }

    return {
      agent1Response: aiResponse.agent1Response || fallback.agent1Response,
      recommendedRecipes: aiResponse.recommendedRecipeIds || aiResponse.recommendedRecipes || []
    };
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || loading) return;

    const userPrompt = input.trim();
    setInput("");
    setStatus({ type: "", message: "" });

    // Append user message
    const userMsgId = Date.now().toString();
    setMessages((prev) => [
      ...prev,
      {
        id: userMsgId,
        sender: "user",
        text: userPrompt
      }
    ]);

    setLoading(true);

    try {
      const aiResponse = await sendChatPrompt(token, userPrompt);
      const normalized = normalizeAiResponse(aiResponse);
      
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 1).toString(),
          sender: "ai",
          text: normalized.agent1Response,
          recommendedRecipes: normalized.recommendedRecipes
        }
      ]);
    } catch (err) {
      setStatus({ type: "error", message: err.message });
      setMessages((prev) => [
        ...prev,
        {
          id: (Date.now() + 2).toString(),
          sender: "ai",
          text: `Error: ${err.message}`
        }
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleIngredientClick = (name) => {
    setInput((prev) => {
      const trimmed = prev.trim();
      if (!trimmed) return `I'd like to cook something with ${name}`;
      return `${trimmed}, ${name}`;
    });
  };

  const resolveRecommendedDetails = (recommendedRecipes = []) => {
    if (!Array.isArray(recommendedRecipes) || recommendedRecipes.length === 0) {
      return [];
    }

    const detailsEntries = Object.values(detailsMap);
    const usedIds = new Set();

    return recommendedRecipes
      .map((recipeIdentifier) => {
        if (!recipeIdentifier) return null;

        const matched = detailsEntries.find((details) => {
          const id = details?.recipe?.recipeId;
          const name = String(details?.recipe?.recipeName || "").trim().toLowerCase();
          
          if (typeof recipeIdentifier === "number" || /^\d+$/.test(recipeIdentifier)) {
            return String(id) === String(recipeIdentifier);
          } else {
            return name === String(recipeIdentifier).trim().toLowerCase();
          }
        });

        if (!matched || usedIds.has(matched.recipe.recipeId)) {
          return null;
        }

        usedIds.add(matched.recipe.recipeId);
        return matched;
      })
      .filter(Boolean);
  };

  const handleViewRecipe = (recipeId) => {
    window.open(`/recipe/${recipeId}`, "_blank");
  };

  return (
    <div className="split-layout">
      <div className="grid" style={{ gap: 16 }}>
        <div className="section-header">
          <div>
            <h2>Chef AI Assistant</h2>
            <p style={{ color: "#6b7280", margin: "4px 0 0 0", fontSize: "14px" }}>
              Get conversational recipe recommendations based on your prompt and your inventory.
            </p>
          </div>
        </div>

        {status.message && status.type === "error" && (
          <div className="alert">{status.message}</div>
        )}

        <div className="chat-container">
          <div className="chat-header">
            <h3>💬 Chat with AI Chef</h3>
          </div>

          <div className="chat-messages">
            {messages.map((msg) => (
              <div
                key={msg.id}
                className={`chat-message ${msg.sender}`}
              >
                <div style={{ whiteSpace: "pre-wrap" }}>{msg.text}</div>

                {/* Render Recipe Cards suggested by Agent 1 */}
                {msg.sender === "ai" && msg.recommendedRecipes && msg.recommendedRecipes.length > 0 && (
                  <div style={{ marginTop: "16px", display: "grid", gap: "12px" }}>
                    {resolveRecommendedDetails(msg.recommendedRecipes).map((details) => {
                      const id = details.recipe.recipeId;
                      return (
                        <RecipeCard
                          key={id}
                          recipe={details.recipe}
                          match={{
                            matchedIngredients: details.matchedIngredients,
                            totalIngredients: details.totalIngredients,
                            matchPercentage: details.matchPercentage
                          }}
                          details={details}
                          onCook={() => handleViewRecipe(id)}
                        />
                      );
                    })}
                  </div>
                )}
              </div>
            ))}

            {loading && (
              <div className="chat-message ai">
                <div className="loading-dots">
                  <span></span>
                  <span></span>
                  <span></span>
                </div>
                <div style={{ fontSize: "12px", color: "#6b7280", marginTop: "4px" }}>
                  AI chefs are analyzing your ingredients...
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <form className="chat-input-area" onSubmit={handleSend}>
            <input
              type="text"
              placeholder="Enter your message (e.g. 'something sweet with apples' or 'a quick lunch')"
              value={input}
              onChange={(e) => setInput(e.target.value)}
              disabled={loading}
            />
            <button type="submit" className="primary-btn" disabled={loading || !input.trim()}>
              Send
            </button>
          </form>
        </div>
      </div>

      <div className="grid" style={{ gap: 16 }}>
        <div className="card">
          <h3>🛒 Your Ingredients</h3>
          <p style={{ color: "#6b7280", fontSize: "13px" }}>
            Click an ingredient to auto-insert it into the chat input.
          </p>

          {inventory.length === 0 ? (
            <p style={{ color: "#9ca3af", fontStyle: "italic", fontSize: "14px" }}>
              You have no ingredients in your inventory. Go to the Inventory page to add some.
            </p>
          ) : (
            <div className="ingredient-chips-container">
              {inventory.map((item) => (
                <button
                  key={item.ingredientId}
                  type="button"
                  className="ingredient-chip"
                  onClick={() => handleIngredientClick(item.ingredientName)}
                >
                  ➕ {item.ingredientName} ({item.quantity} {item.measurementUnit})
                </button>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
