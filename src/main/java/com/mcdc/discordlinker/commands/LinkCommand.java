package com.mcdc.discordlinker.commands;

import com.mcdc.discordlinker.DiscordLinker;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class LinkCommand implements CommandExecutor {

    private final DiscordLinker plugin;
    
    public LinkCommand(DiscordLinker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.player-only", "&cBu komutu sadece oyuncular kullanabilir!"));
            return true;
        }
        
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        
        String discordId = plugin.getDatabaseManager().getDiscordIdByUUID(uuid);
        
        if (discordId != null) {
            plugin.sendMessage(player, plugin.getConfig().getString("messages.already-linked", "&cHesabınız zaten bir Discord hesabıyla eşlenmiş!"));
            return true;
        }
        
        String code = plugin.getDiscordBot().generateLinkCode(player);
        
        String message = plugin.getConfig().getString("messages.link-code", "&aDiscord hesabınızı eşlemek için, &e/esle &akomutunu kullandınız.\n&aEşleme kodunuz: &e{code}\n&aKodunuzu Discord'da &ehesap-esle &akanalındaki butona tıklayarak açılan pencereye girin.");
        message = message.replace("{code}", code);
        
        plugin.sendMessage(player, message);
        
        return true;
    }
}