package com.mds.recipediscovery.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mds.recipediscovery.dto.RecipeMatchDTO;
import com.mds.recipediscovery.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AiService {

    private static final Logger logger = LoggerFactory.getLogger(AiService.class);

    private final LlmClient llmClient;
    private final ChatToolsService chatToolsService;
    private final ObjectMapper objectMapper;

    public AiService(LlmClient llmClient,
                      ChatToolsService chatToolsService,
                      ObjectMapper objectMapper) {
        this.llmClient = llmClient;
        this.chatToolsService = chatToolsService;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> getChatResponse(Integer userId, String userPrompt) {
        User user = chatToolsService.findUserOrThrow(userId);

        String inventoryStr = chatToolsService.inventorySnapshot(userId);
        List<RecipeMatchDTO> topSubset = chatToolsService.topRecipesForPrompt(userId, userPrompt, 5);
        String recipesContext = chatToolsService.recipeContext(topSubset);

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
                "2. Recommend recipes EXCLUSIVELY from the list of relevant options above. Explain why they fit the user request and user inventory. Do not invent new recipes that are not in the list. Refer to the recommended recipes ONLY by their Names. Do NOT specify, mention, or write the recipe IDs in your conversational reply (`agent1Response`). Write a friendly, helpful conversational reply detailing these choices. Keep it in English or Romanian, matching the user's language query. If the user writes in Romanian, reply in Romanian.\n" +
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

            resultMap.put("agent1Response", jsonNode.path("agent1Response").asText(""));
            resultMap.put("recommendedRecipeIds", relaxedMapper.convertValue(
                    jsonNode.path("recommendedRecipeIds"), List.class));

            return resultMap;
        } catch (Exception e) {
            logger.warn("Standard Jackson parsing failed for LLM response, trying regex parsing. Response: " + normalizedResponse, e);
            try {
                Map<String, Object> resultMap = parseMalformedJson(normalizedResponse);
                if (resultMap.get("agent1Response") != null && !resultMap.get("agent1Response").toString().isEmpty()) {
                    return resultMap;
                }
            } catch (Exception ex) {
                logger.error("Regex parsing also failed", ex);
            }

            // Revert to displaying text if all parsers failed
            Map<String, Object> fallbackMap = new HashMap<>();
            fallbackMap.put("agent1Response", rawLlmResponse);
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
        return text.trim();
    }
}
