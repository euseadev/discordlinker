package com.mcdc.discordlinker.commands;

import com.mcdc.discordlinker.DiscordLinker;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EsleAdminCommand implements CommandExecutor, TabCompleter {

    private final DiscordLinker plugin;
    private final List<String> subCommands = Arrays.asList("unlink", "check", "reload");
    
    public EsleAdminCommand(DiscordLinker plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("discordlinker.admin")) {
            plugin.sendMessage(sender, plugin.getConfig().getString("messages.no-permission", "&cBu komutu kullanmak için yetkiniz yok!"));
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "unlink":
                return handleUnlink(sender, args);
            case "check":
                return handleCheck(sender, args);
            case "reload":
                return handleReload(sender);
            default:
                sendHelp(sender);
                return true;
        }
    }
    
    private boolean handleUnlink(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, "&cKullanım: /esleadmin unlink <oyuncu>");
            return true;
        }
        
        String playerName = args[1];
        
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        UUID uuid;
        
        if (onlinePlayer != null) {
            uuid = onlinePlayer.getUniqueId();
        } else {

            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            uuid = offlinePlayer.getUniqueId();
        }
        

        String discordId = plugin.getDatabaseManager().getDiscordIdByUUID(uuid);
        
        if (discordId == null) {
            plugin.sendMessage(sender, "&c" + playerName + " adlı oyuncunun hesabı eşlenmemiş!");
            return true;
        }
        

        boolean success = plugin.getDatabaseManager().unlinkAccounts(uuid, discordId);
        
        if (success) {
            plugin.sendMessage(sender, "&a" + playerName + " adlı oyuncunun hesap eşlemesi kaldırıldı!");
            

            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                plugin.sendMessage(player, plugin.getConfig().getString("messages.unlink-success", "&cHesabınızın Discord eşlemesi bir yetkili tarafından kaldırıldı!"));
            }
        } else {
            plugin.sendMessage(sender, "&cHesap eşlemesi kaldırılırken bir hata oluştu!");
        }
        
        return true;
    }
    
    private boolean handleCheck(CommandSender sender, String[] args) {
        if (args.length < 2) {
            plugin.sendMessage(sender, "&cKullanım: /esleadmin check <oyuncu>");
            return true;
        }
        
        String playerName = args[1];
        
        Player onlinePlayer = Bukkit.getPlayer(playerName);
        UUID uuid;
        
        if (onlinePlayer != null) {
            uuid = onlinePlayer.getUniqueId();
        } else {

            @SuppressWarnings("deprecation")
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
            uuid = offlinePlayer.getUniqueId();
        }
        

        String discordId = plugin.getDatabaseManager().getDiscordIdByUUID(uuid);
        
        if (discordId == null) {
            plugin.sendMessage(sender, "&c" + playerName + " adlı oyuncunun hesabı eşlenmemiş!");
            return true;
        }
        
        plugin.sendMessage(sender, "&a" + playerName + " adlı oyuncunun Discord ID'si: &e" + discordId);
        return true;
    }
    
    private boolean handleReload(CommandSender sender) {
        plugin.reloadPlugin();
        plugin.sendMessage(sender, "&aEklenti yapılandırması yeniden yüklendi!");
        return true;
    }
    

    
    private void sendHelp(CommandSender sender) {
        plugin.sendMessage(sender, "&6=== DiscordLinker Admin Komutları ===");
        plugin.sendMessage(sender, "&e/esleadmin unlink <oyuncu> &7- Oyuncunun hesap eşlemesini kaldırır");
        plugin.sendMessage(sender, "&e/esleadmin check <oyuncu> &7- Oyuncunun hesap eşlemesini kontrol eder");
        plugin.sendMessage(sender, "&e/esleadmin reload &7- Eklenti yapılandırmasını yeniden yükler");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("discordlinker.admin")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("unlink") || args[0].equalsIgnoreCase("check")) {
                return null; // Oyuncu listesini göster
            }
        }
        
        return new ArrayList<>();
    }
}