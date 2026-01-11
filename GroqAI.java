package com.jeffai.ai;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;
import org.bukkit.ChatColor;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class GroqAI {
    private final String apiKey;
    private final String model;
    private final OkHttpClient client;
    private final Gson gson;
    private final String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
    
    public GroqAI(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model != null ? model : "llama3-8b-8192";
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }
    
    public String chat(String message, String playerName) throws IOException {
        if (apiKey == null || apiKey.isEmpty()) {
            return ChatColor.RED + "API key not configured! Use /jeff setkey <your-api-key>";
        }
        
        // Create request body
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are Jeff, a friendly AI assistant in Minecraft. " +
            "You're talking to " + playerName + ". Keep responses short (1-2 sentences max), " +
            "Minecraft-themed when appropriate, and friendly. Don't use markdown.");
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", message);
        
        requestBody.put("messages", new Map[]{systemMessage, userMessage});
        requestBody.put("max_tokens", 150);
        requestBody.put("temperature", 0.7);
        
        String jsonBody = gson.toJson(requestBody);
        
        // Create request
        Request request = new Request.Builder()
            .url(apiUrl)
            .addHeader("Authorization", "Bearer " + apiKey)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(jsonBody, MediaType.get("application/json")))
            .build();
        
        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                return ChatColor.RED + "Error: " + response.code() + " - " + response.message();
            }
            
            String responseBody = response.body().string();
            JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
            
            if (jsonResponse.has("choices") && jsonResponse.getAsJsonArray("choices").size() > 0) {
                String content = jsonResponse.getAsJsonArray("choices")
                    .get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
                
                return ChatColor.translateAlternateColorCodes('&', 
                    "&6[Jeff] &f" + content.trim());
            } else {
                return ChatColor.RED + "Error: No response from AI";
            }
        } catch (Exception e) {
            return ChatColor.RED + "Error: " + e.getMessage();
        }
    }
    
    public void setApiKey(String apiKey) {
        // This would update the config in the main plugin
        JeffPlugin.getInstance().getConfig().set("groq-api-key", apiKey);
        JeffPlugin.getInstance().saveConfig();
    }
    
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isEmpty();
    }
}