package com.jeffai.commands;

import com.jeffai.ai.GroqAI;
import com.jeffai.JeffPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;

public class JeffCommand implements CommandExecutor {
    private final JeffPlugin plugin;
    private final GroqAI groqAI;
    
    public JeffCommand(JeffPlugin plugin, GroqAI groqAI) {
        this.plugin = plugin;
        this.groqAI = groqAI;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            player.sendMessage(ChatColor.GOLD + "Usage: /" + label + " <message>");
            player.sendMessage(ChatColor.GOLD + "Or: /" + label + " setkey <your-api-key>");
            return true;
        }
        
        // Handle API key setting
        if (args[0].equalsIgnoreCase("setkey") && args.length >= 2) {
            if (!player.hasPermission("jeff.setkey")) {
                player.sendMessage(ChatColor.RED + "You don't have permission to set the API key!");
                return true;
            }
            
            groqAI.setApiKey(args[1]);
            player.sendMessage(ChatColor.GREEN + "API key updated successfully!");
            return true;
        }
        
        // Handle chat message
        String message = String.join(" ", args);
        
        // Check rate limiting (simple implementation)
        if (!checkRateLimit(player)) {
            player.sendMessage(ChatColor.RED + "Please wait before sending another message!");
            return true;
        }
        
        // Send typing indicator
        player.sendMessage(ChatColor.GRAY + "Jeff is thinking...");
        
        // Process in async task to avoid blocking main thread
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String response = groqAI.chat(message, player.getName());
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(response);
                });
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "Error communicating with AI: " + e.getMessage());
                });
            }
        });
        
        return true;
    }
    
    private boolean checkRateLimit(Player player) {
        // Simple rate limiting - in production, you'd want to store timestamps
        // This is a basic implementation
        if (player.hasPermission("jeff.bypassratelimit")) {
            return true;
        }
        
        // Check if player has messaged in last 5 seconds
        // This would need proper implementation with cooldown storage
        return true;
    }
}