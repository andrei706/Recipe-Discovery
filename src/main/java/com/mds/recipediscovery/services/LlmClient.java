package com.mds.recipediscovery.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class LlmClient {

    private static final Logger logger = LoggerFactory.getLogger(LlmClient.class);

    private final ObjectMapper objectMapper;

    @Value("${ai.provider:mock}")
    private String provider;

    @Value("${langchain4j.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${langchain4j.gemini.model-name:gemini-1.5-flash}")
    private String geminiModelName;

    @Value("${langchain4j.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    @Value("${langchain4j.ollama.model-name:llama3}")
    private String ollamaModelName;

    private ChatModel geminiModel;
    private ChatModel ollamaModel;

    public LlmClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private synchronized ChatModel getGeminiModel() {
        if (geminiModel == null) {
            if (geminiApiKey == null || geminiApiKey.trim().isEmpty()) {
                logger.warn("Gemini API key is not configured. GoogleAiGeminiChatModel may fail to initialize or execute.");
            }
            geminiModel = GoogleAiGeminiChatModel.builder()
                    .apiKey(geminiApiKey)
                    .modelName(geminiModelName)
                    .build();
        }
        return geminiModel;
    }

    private synchronized ChatModel getOllamaModel() {
        if (ollamaModel == null) {
            ollamaModel = OllamaChatModel.builder()
                    .baseUrl(ollamaBaseUrl)
                    .modelName(ollamaModelName)
                    .build();
        }
        return ollamaModel;
    }

    public String generate(String systemPrompt, String userPrompt) {
        if ("mock".equalsIgnoreCase(provider)) {
            return generateMockResponse(systemPrompt, userPrompt);
        }

        try {
            ChatModel model;
            if ("gemini".equalsIgnoreCase(provider)) {
                model = getGeminiModel();
            } else if ("ollama".equalsIgnoreCase(provider)) {
                model = getOllamaModel();
            } else {
                logger.warn("Unknown AI provider '{}'. Falling back to Mock LLM provider.", provider);
                return generateMockResponse(systemPrompt, userPrompt);
            }

            ChatRequest chatRequest = ChatRequest.builder()
                    .messages(
                            SystemMessage.from(systemPrompt),
                            UserMessage.from(userPrompt)
                    )
                    .build();

            ChatResponse response = model.chat(chatRequest);
            if (response != null && response.aiMessage() != null) {
                return response.aiMessage().text();
            } else {
                throw new RuntimeException("Null response from LangChain4j model");
            }
        } catch (Exception e) {
            logger.error("Error communicating through LangChain4j provider '{}', falling back to mock provider", provider, e);
            return generateMockResponse(systemPrompt, userPrompt);
        }
    }

    private String generateMockResponse(String systemPrompt, String userPrompt) {
        logger.info("Using dynamic mock response generator.");

        Pattern pattern = Pattern.compile("ID:\\s*(\\d+)\\s*\\|\\s*Name:\\s*([^|\\n]+)");
        Matcher matcher = pattern.matcher(systemPrompt);
        List<Integer> ids = new ArrayList<>();
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            ids.add(Integer.parseInt(matcher.group(1)));
            names.add(matcher.group(2).trim());
        }

        if (systemPrompt.contains("meal planning") || systemPrompt.contains("mealType")) {
            int numDays = 7;
            Pattern dayPattern = Pattern.compile("structured meal plan for (\\d+) day");
            Matcher dayMatcher = dayPattern.matcher(systemPrompt);
            if (dayMatcher.find()) {
                try {
                    numDays = Integer.parseInt(dayMatcher.group(1));
                } catch (Exception ignored) {}
            }

            List<String> assignments = new ArrayList<>();
            String[] meals = {"breakfast", "lunch", "dinner", "snack"};
            int recipeIdx = 0;
            if (!ids.isEmpty()) {
                for (int day = 1; day <= numDays; day++) {
                    for (String meal : meals) {
                        int recipeId = ids.get(recipeIdx % ids.size());
                        assignments.add(String.format("{\"dayNumber\":%d,\"mealType\":\"%s\",\"recipeId\":%d}", day, meal, recipeId));
                        recipeIdx++;
                    }
                }
            }
            return "[" + String.join(",", assignments) + "]";
        }

        if (ids.isEmpty()) {
            return "{\n" +
                    "  \"agent1Response\": \"I couldn't find any specific recipes in the database matching your search, but I can help you search for other options!\",\n" +
                    "  \"recommendedRecipeIds\": []\n" +
                    "}";
        }

        // Recommend the top matching recipes from the parsed list
        int count = Math.min(2, ids.size());
        StringBuilder recNames = new StringBuilder();
        List<Integer> recIds = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (i > 0)
                recNames.append(", ");
            recNames.append("**").append(names.get(i)).append("**");
            recIds.add(ids.get(i));
        }

        String idsStr = recIds.stream().map(String::valueOf).collect(Collectors.joining(", "));

        return String.format(
                "{\n" +
                        "  \"agent1Response\": \"I found the following recipe for you: %s! It aligns well with the available ingredients in your inventory.\",\n" +
                        "  \"recommendedRecipeIds\": [%s]\n" +
                        "}",
                recNames.toString(), idsStr);
    }
}
