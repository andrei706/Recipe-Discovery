package com.mds.recipediscovery.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.Diet;
import com.mds.recipediscovery.models.Recipe;
import com.mds.recipediscovery.models.User;
import com.mds.recipediscovery.repository.RecipeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final LlmClient llmClient;
    private final ChatToolsService chatToolsService;
    private final ObjectMapper objectMapper;
    private final RecipeRepository recipeRepository;

    public AiService(LlmClient llmClient,
                      ChatToolsService chatToolsService,
                      ObjectMapper objectMapper,
                      RecipeRepository recipeRepository) {
        this.llmClient = llmClient;
        this.chatToolsService = chatToolsService;
        this.objectMapper = objectMapper;
        this.recipeRepository = recipeRepository;
    }

    /**
     * Generates a meal plan assignment list using the LLM.
     * Returns a list of maps, each with keys: dayNumber (int), mealType (String), recipeId (int).
     */
    public List<Map<String, Object>> generateMealPlan(Integer userId, String userPrompt, int numDays) {
        // Build a full recipe catalogue for context
        List<Recipe> allRecipes = recipeRepository.findAll();
        String recipeCatalogue = allRecipes.stream()
                .map(r -> {
                    String dietsStr = r.getDietClassifications() == null ? "" :
                            r.getDietClassifications().stream().map(Diet::getName).collect(Collectors.joining(", "));
                    String featuresStr = r.getFeatures() == null ? "" :
                            String.join(", ", r.getFeatures());
                    return String.format("ID: %d | Name: %s | Calories: %.0f kcal | Macros: Carbs: %.1fg, Fats: %.1fg, Protein: %.1fg | Diets: %s | Tags: %s | Prep: %d min",
                            r.getRecipeId(), r.getName(), r.getCaloriesKcal(),
                            r.getCarbohydratesG(), r.getFatsG(), r.getProteinsG(),
                            dietsStr, featuresStr, r.getTotalPrepTimeMinutes());
                })
                .collect(Collectors.joining("\n"));

        String inventoryStr = chatToolsService.inventorySnapshot(userId);

        String systemPrompt = "You are a professional meal planning assistant.\n" +
                "Your job is to create a structured meal plan for " + numDays + " day(s) using ONLY the recipes listed below.\n\n" +
                "Available Recipes in the database:\n" + recipeCatalogue + "\n\n" +
                "User's current inventory:\n" + (inventoryStr.isEmpty() ? "(none)" : inventoryStr) + "\n\n" +
                "Rules:\n" +
                "1. Assign meals using ONLY recipe IDs from the list above.\n" +
                "2. Each day has 4 meal slots: breakfast, lunch, dinner, snack. You do NOT need to fill all of them — skip slots that don't fit naturally.\n" +
                "3. Each meal slot per day can have at most ONE recipe.\n" +
                "4. STRICT DIETARY ADHERENCE: You MUST strictly adhere to the user's requested diet (e.g., 'Keto only', 'Vegetarian', 'Vegan', etc.) and other preferences in the prompt. If the user asks for a specific diet like 'Keto' (which should be extremely low-carb, high-fat, and moderate-protein), combine this diet restriction with any other requirements they specify in their prompt. NEVER assign a recipe that violates the requested diet. If there are not enough compliant recipes, leave the slots empty. DO NOT fill slots with non-compliant recipes.\n" +
                "5. Do NOT repeat the same recipe in the same day.\n" +
                "6. Respond ONLY with a valid JSON object map where keys are day numbers (1 to " + numDays + ") and values are objects mapping meal slots to recipe IDs. No explanation outside the JSON.\n" +
                "The JSON must be exactly structured like:\n" +
                "{\n" +
                "  \"1\": {\n" +
                "    \"breakfast\": 5,\n" +
                "    \"lunch\": 12,\n" +
                "    \"dinner\": 8\n" +
                "  },\n" +
                "  \"2\": {\n" +
                "    \"breakfast\": 2,\n" +
                "    \"dinner\": 12\n" +
                "  }\n" +
                "}\n" +
                "Valid meal slot values: breakfast, lunch, dinner, snack.";

        String rawResponse = llmClient.generate(systemPrompt, userPrompt);
        String cleaned = cleanJsonResponse(rawResponse);

        List<Map<String, Object>> result = new ArrayList<>();
        try {
            JsonNode root = objectMapper.readTree(cleaned);
            if (root.isArray()) {
                for (JsonNode item : root) {
                    parseItem(item, result, numDays);
                }
            } else if (root.isObject()) {
                // Check if it has an array field (e.g. wrapper object)
                boolean foundArray = false;
                Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    if (entry.getValue().isArray()) {
                        for (JsonNode item : entry.getValue()) {
                            parseItem(item, result, numDays);
                        }
                        foundArray = true;
                        break;
                    }
                }

                if (!foundArray) {
                    // It's a map keyed by day: {"1": {"breakfast": 5}, "day 2": {"lunch": 12}}
                    fields = root.fields();
                    while (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String key = entry.getKey().toLowerCase();
                        JsonNode val = entry.getValue();

                        int dayNum = extractDayNumber(key);
                        if (dayNum >= 1 && dayNum <= numDays && val.isObject()) {
                            Iterator<Map.Entry<String, JsonNode>> mealFields = val.fields();
                            while (mealFields.hasNext()) {
                                Map.Entry<String, JsonNode> mealEntry = mealFields.next();
                                String mealType = mealEntry.getKey().toLowerCase().trim();
                                if (List.of("breakfast", "lunch", "dinner", "snack").contains(mealType)) {
                                    int recipeId = extractRecipeId(mealEntry.getValue());
                                    if (recipeId > 0) {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("dayNumber", dayNum);
                                        map.put("mealType", mealType);
                                        map.put("recipeId", recipeId);
                                        result.add(map);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to parse meal plan JSON from LLM: {}", cleaned, e);
        }
        return result;
    }

    private int extractDayNumber(String key) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(key);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group());
            } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private int extractRecipeId(JsonNode node) {
        if (node.isNumber()) {
            return node.asInt();
        } else if (node.isObject()) {
            if (node.has("recipeId")) return node.get("recipeId").asInt();
            if (node.has("id")) return node.get("id").asInt();
        }
        return -1;
    }

    private void parseItem(JsonNode item, List<Map<String, Object>> result, int numDays) {
        int day = -1;
        if (item.has("dayNumber")) day = item.get("dayNumber").asInt();
        else if (item.has("day")) day = item.get("day").asInt();

        String mealType = "";
        if (item.has("mealType")) mealType = item.get("mealType").asText().toLowerCase().trim();
        else if (item.has("meal")) mealType = item.get("meal").asText().toLowerCase().trim();

        int recipeId = -1;
        if (item.has("recipeId")) recipeId = item.get("recipeId").asInt();
        else if (item.has("recipe_id")) recipeId = item.get("recipe_id").asInt();
        else if (item.has("id")) recipeId = item.get("id").asInt();

        if (day >= 1 && day <= numDays && recipeId > 0 &&
                List.of("breakfast", "lunch", "dinner", "snack").contains(mealType)) {
            Map<String, Object> map = new HashMap<>();
            map.put("dayNumber", day);
            map.put("mealType", mealType);
            map.put("recipeId", recipeId);
            result.add(map);
    }
    }


    public Map<String, Object> getChatResponse(Integer userId, String userPrompt) {
        User user = chatToolsService.findUserOrThrow(userId);

        String inventoryStr = chatToolsService.inventorySnapshot(userId);
        List<RecipeMatchDTO> topSubset = chatToolsService.topRecipesForPrompt(userId, userPrompt, 5);
        String recipesContext = chatToolsService.recipeContext(userId, topSubset);

        String systemPrompt = "You are an intelligent kitchen assistant orchestrated with backend tools (inventory tool, recipe-search tool, and nutrition context tool):\n" +
                "Your role is to suggest database recipes based on user queries and available ingredients.\n\n" +
                "User Profile:\n" +
                "- Name: " + user.getUsername() + "\n" +
                "Available Ingredients in User's Inventory:\n" +
                (inventoryStr.isEmpty() ? "(No ingredients in inventory)" : inventoryStr) + "\n\n" +
                "In our database, we have ONLY these relevant options matching your query:\n" +
                (recipesContext.isEmpty() ? "(No matching recipes found in database)" : recipesContext) + "\n" +
                "Instructions:\n" +
                "1. Read the user's prompt carefully.\n" +
                "2. Recommend recipes EXCLUSIVELY from the list of relevant options above. Explain why they fit the user request and user inventory. Do not invent new recipes that are not in the list. Refer to the recommended recipes ONLY by their Names. Do NOT specify, mention, or write the recipe IDs in your conversational reply (`agent1Response`). Do NOT use any markdown formatting in your conversational reply (`agent1Response`), especially bold formatting (avoid wrapping recipe names or any words in asterisks like **Recipe Name** or *Recipe Name*). Write only plain text. Write a friendly, helpful conversational reply detailing these choices. Keep it in English or Romanian, matching the user's language query. If the user writes in Romanian, reply in Romanian. IMPORTANT: If a recipe has missing or insufficient ingredients (indicated with '[Insufficient...]' or '[Missing...]' in the list of relevant options above), you MUST still recommend the recipe, but you MUST explicitly mention to the user that they do not have enough of those ingredients, stating what they currently have in inventory vs. what is required.\n" +
                "3. You MUST respond with ONLY a valid JSON object. Do not include any explanation outside the JSON. The JSON structure MUST be exactly:\n" +
                "{\n" +
                "  \"agent1Response\": \"Conversational chat response recommending recipes EXCLUSIVELY from the database list...\",\n" +
                "  \"recommendedRecipeIds\": [number, number]\n" +
                "}\n" +
                "Use the exact database recipe IDs for recommendedRecipeIds if you suggest any from the database list above." +
                "NOTE: if the user inputs a prompt that is not related to the database or the kitchen, tell him to give a prompt related to the topic. If he just showed gratitude in the unrelated prompt, reply similary.";

        String rawLlmResponse = llmClient.generate(systemPrompt, userPrompt);
        String cleanedResponse = cleanJsonResponse(rawLlmResponse);
        String normalizedResponse = normalizeNestedJsonResponse(cleanedResponse);

        ObjectMapper relaxedMapper = objectMapper.copy()
                .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)
                .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_SINGLE_QUOTES, true)
                .configure(com.fasterxml.jackson.core.JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);

        try {
            JsonNode jsonNode = relaxedMapper.readTree(normalizedResponse);
            Map<String, Object> resultMap = new HashMap<>();

            String agent1Response = jsonNode.path("agent1Response").asText("");
            if (agent1Response != null) {
                agent1Response = agent1Response.replace("**", "");
            }
            resultMap.put("agent1Response", agent1Response);
            resultMap.put("recommendedRecipeIds", relaxedMapper.convertValue(
                    jsonNode.path("recommendedRecipeIds"), List.class));

            return resultMap;
        } catch (Exception e) {
            logger.warn("Standard Jackson parsing failed for LLM response, trying regex parsing. Response: " + normalizedResponse, e);
            try {
                Map<String, Object> resultMap = parseMalformedJson(normalizedResponse);
                if (resultMap.get("agent1Response") != null && !resultMap.get("agent1Response").toString().isEmpty()) {
                    String agent1 = resultMap.get("agent1Response").toString().replace("**", "");
                    resultMap.put("agent1Response", agent1);
                    return resultMap;
                }
            } catch (Exception ex) {
                logger.error("Regex parsing also failed", ex);
            }

            // Revert to displaying text if all parsers failed
            Map<String, Object> fallbackMap = new HashMap<>();
            String fallbackResponse = rawLlmResponse != null ? rawLlmResponse.replace("**", "") : "";
            fallbackMap.put("agent1Response", fallbackResponse);
            fallbackMap.put("recommendedRecipeIds", List.of());
            return fallbackMap;
        }
    }

    private Map<String, Object> parseMalformedJson(String json) {
        Map<String, Object> resultMap = new HashMap<>();

        String agent1 = extractField(json, "agent1Response");
        resultMap.put("agent1Response", agent1);

        List<Integer> ids = new ArrayList<>();
        Pattern idPattern = Pattern.compile("\"recommendedRecipeIds\"\\s*:\\s*\\[([^\\]]*)\\]", Pattern.CASE_INSENSITIVE);
        Matcher idMatcher = idPattern.matcher(json);
        if (idMatcher.find()) {
            String idsListStr = idMatcher.group(1);
            Pattern numPattern = Pattern.compile("\\d+");
            Matcher numMatcher = numPattern.matcher(idsListStr);
            while (numMatcher.find()) {
                ids.add(Integer.parseInt(numMatcher.group()));
            }
        }
        resultMap.put("recommendedRecipeIds", ids);

        return resultMap;
    }

    private String extractField(String json, String fieldName) {
        int keyIndex = json.indexOf("\"" + fieldName + "\"");
        if (keyIndex == -1) return "";

        int colonIndex = json.indexOf(":", keyIndex + fieldName.length() + 2);
        if (colonIndex == -1) return "";

        int valStart = colonIndex + 1;
        while (valStart < json.length() && Character.isWhitespace(json.charAt(valStart))) {
            valStart++;
        }
        if (valStart >= json.length()) return "";

        if (json.charAt(valStart) == '"') {
            StringBuilder sb = new StringBuilder();
            boolean escaped = false;
            for (int i = valStart + 1; i < json.length(); i++) {
                char c = json.charAt(i);
                if (escaped) {
                    sb.append(c);
                    escaped = false;
                } else if (c == '\\') {
                    escaped = true;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        } else {
            int valEnd = valStart;
            int braces = 0;
            for (; valEnd < json.length(); valEnd++) {
                char c = json.charAt(valEnd);
                if (c == '{') braces++;
                if (c == '}') {
                    if (braces == 0) break;
                    braces--;
                }
                if (c == ',' && braces == 0) {
                    break;
                }
            }
            String rawVal = json.substring(valStart, valEnd).trim();
            if (rawVal.endsWith("}")) {
                int countOpen = 0;
                for (int i = 0; i < rawVal.length(); i++) {
                    if (rawVal.charAt(i) == '{') countOpen++;
                    if (rawVal.charAt(i) == '}') countOpen--;
                }
                if (countOpen < 0) {
                    rawVal = rawVal.substring(0, rawVal.length() - 1).trim();
                }
            }
            return rawVal;
        }
    }

    private String normalizeNestedJsonResponse(String cleanedResponse) {
        try {
            JsonNode parsedNode = objectMapper.readTree(cleanedResponse);
            if (parsedNode.isObject()
                    && parsedNode.has("agent1Response")
                    && parsedNode.path("recommendedRecipeIds").isMissingNode()) {
                String nestedJson = parsedNode.path("agent1Response").asText("");
                if (!nestedJson.isBlank()) {
                    String nestedCleanedJson = cleanJsonResponse(nestedJson);
                    JsonNode nestedNode = objectMapper.readTree(nestedCleanedJson);
                    if (nestedNode.isObject() && nestedNode.has("agent1Response")) {
                        return nestedNode.toString();
                    }
                }
            }
        } catch (Exception ignored) {
            // Keep original cleaned response when it is not nested JSON.
        }
        return cleanedResponse;
    }

    private String cleanJsonResponse(String text) {
        if (text == null) return "";
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        } else if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        text = text.trim();

        int firstBrace = text.indexOf('{');
        int firstBracket = text.indexOf('[');

        int start = -1;
        char endChar = ' ';
        if (firstBrace != -1 && (firstBracket == -1 || firstBrace < firstBracket)) {
            start = firstBrace;
            endChar = '}';
        } else if (firstBracket != -1) {
            start = firstBracket;
            endChar = ']';
        }

        if (start != -1) {
            int lastEnd = text.lastIndexOf(endChar);
            if (lastEnd > start) {
                return text.substring(start, lastEnd + 1).trim();
            }
        }

        return text;
    }
}
