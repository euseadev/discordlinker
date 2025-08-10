package com.mcdc.discordlinker.listeners;

import com.mcdc.discordlinker.DiscordLinker;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final DiscordLinker plugin;
    
    public PlayerListener(DiscordLinker plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        

        if (!player.hasPlayedBefore() || !plugin.getLinkManager().isLinked(player.getUniqueId())) {

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline() && !plugin.getLinkManager().isLinked(player.getUniqueId())) {
                    String message = plugin.getConfig().getString("messages.link-reminder", 
                            "&eDiscord hesabınızı Minecraft hesabınızla eşlemek için &a/esle &ekomutunu kullanabilirsiniz!");
                    plugin.sendMessage(player, message);
                }
            }, 60L); // 3 saniye sonra
        }
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {

    }
}