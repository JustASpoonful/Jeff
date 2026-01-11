package com.jeffai;

import org.bukkit.plugin.java.JavaPlugin;
import com.jeffai.ai.GroqAI;
import com.jeffai.commands.JeffCommand;

public class JeffPlugin extends JavaPlugin {
    private static JeffPlugin instance;
    private GroqAI groqAI;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Save default config
        saveDefaultConfig();
        
        // Initialize Groq AI
        String apiKey = getConfig().getString("groq-api-key");
        String model = getConfig().getString("groq-model");
        
        if (apiKey == null || apiKey.isEmpty()) {
            getLogger().warning("Groq API key not set in config.yml!");
            getLogger().warning("Players will be prompted to set their own API key using /jeff setkey <key>");
        }
        
        groqAI = new GroqAI(apiKey, model);
        
        // Register command
        String cmd = getConfig().getString("command", "jeff");
        getCommand(cmd).setExecutor(new JeffCommand(this, groqAI));
        
        getLogger().info("JeffAI v" + getDescription().getVersion() + " has been enabled!");
        getLogger().info("Use /" + cmd + " <message> to chat with Jeff AI!");
    }
    
    @Override
    public void onDisable() {
        getLogger().info("JeffAI has been disabled!");
    }
    
    public static JeffPlugin getInstance() {
        return instance;
    }
    
    public GroqAI getGroqAI() {
        return groqAI;
    }
}