package com.mcdc.discordlinker.managers;

import com.mcdc.discordlinker.DiscordLinker;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class LinkManager {

    private final DiscordLinker plugin;
    
    public LinkManager(DiscordLinker plugin) {
        this.plugin = plugin;
        
        // Süresi dolmuş kodları temizlemek için zamanlayıcı başlat
        startCleanupTask();
    }
    
    private void startCleanupTask() {
        int interval = plugin.getConfig().getInt("settings.cleanup-interval", 5) * 60 * 20; // Dakika -> Tick
        
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getDatabaseManager().cleanupExpiredCodes();
        }, interval, interval);
    }
    
    public boolean isLinked(UUID uuid) {
        return plugin.getDatabaseManager().getDiscordIdByUUID(uuid) != null;
    }
    
    public boolean isLinked(String discordId) {
        return plugin.getDatabaseManager().getUUIDByDiscordId(discordId) != null;
    }
    
    public String getDiscordId(UUID uuid) {
        return plugin.getDatabaseManager().getDiscordIdByUUID(uuid);
    }
    
    public UUID getPlayerUUID(String discordId) {
        return plugin.getDatabaseManager().getUUIDByDiscordId(discordId);
    }
    
    public String getPlayerName(String discordId) {
        return plugin.getDatabaseManager().getUsernameByDiscordId(discordId);
    }
    
    public boolean linkAccounts(UUID uuid, String username, String discordId) {
        boolean success = plugin.getDatabaseManager().linkAccounts(uuid, username, discordId);
        
        if (success) {
    
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.sendMessage(player, plugin.getConfig().getString("messages.link-success", "&aHesabınız başarıyla Discord hesabınızla eşlendi!"));
            }
        }
        
        return success;
    }
    
    public boolean unlinkAccounts(UUID uuid, String discordId) {
        return plugin.getDatabaseManager().unlinkAccounts(uuid, discordId);
    }
}