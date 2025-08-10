package com.mcdc.discordlinker;

import com.mcdc.discordlinker.commands.EsleAdminCommand;
import com.mcdc.discordlinker.commands.EsleCommand;
import com.mcdc.discordlinker.database.DatabaseManager;
import com.mcdc.discordlinker.discord.DiscordBot;
import com.mcdc.discordlinker.listeners.PlayerListener;
import com.mcdc.discordlinker.managers.LinkManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public final class DiscordLinker extends JavaPlugin {

    private static DiscordLinker instance;
    private DiscordBot discordBot;
    private DatabaseManager databaseManager;
    private LinkManager linkManager;
    private FileConfiguration config;

    @Override
    public void onEnable() {
        instance = this;
        
        saveDefaultConfig();
        config = getConfig();
        
        setupDatabase();
        
        linkManager = new LinkManager(this);
        
        setupDiscordBot();
        
        getCommand("esle").setExecutor(new EsleCommand(this));
        getCommand("esleadmin").setExecutor(new EsleAdminCommand(this));
        
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        
        getLogger().info("DiscordLinker eklentisi başarıyla etkinleştirildi!");
    }

    @Override
    public void onDisable() {
        if (discordBot != null) {
            discordBot.shutdown();
        }
        
        if (databaseManager != null) {
            databaseManager.close();
        }
        
        getLogger().info("DiscordLinker eklentisi kapatıldı!");
    }
    
    private void setupDatabase() {
        String dbType = config.getString("database.type", "sqlite");
        
        if (dbType.equalsIgnoreCase("sqlite")) {
            String dbFile = config.getString("database.sqlite-file", "discordlinker.db");
            File dataFolder = getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            
            databaseManager = new DatabaseManager(this, dbType, new File(dataFolder, dbFile).getAbsolutePath());
        } else if (dbType.equalsIgnoreCase("mysql")) {
            String host = config.getString("database.mysql.host", "localhost");
            int port = config.getInt("database.mysql.port", 3306);
            String database = config.getString("database.mysql.database", "discordlinker");
            String username = config.getString("database.mysql.username", "root");
            String password = config.getString("database.mysql.password", "password");
            
            databaseManager = new DatabaseManager(this, dbType, host, port, database, username, password);
        } else {
            getLogger().warning("Bilinmeyen veritabanı tipi: " + dbType + ". SQLite kullanılacak.");
            databaseManager = new DatabaseManager(this, "sqlite", new File(getDataFolder(), "discordlinker.db").getAbsolutePath());
        }
        
        databaseManager.initialize();
    }
    
    private void setupDiscordBot() {
        String token = config.getString("discord.token");
        if (token == null || token.equals("BOT_TOKEN_BURAYA")) {
            getLogger().warning("Discord bot token'ı ayarlanmamış! Lütfen config.yml dosyasını düzenleyin.");
            return;
        }
        
        String guildId = config.getString("discord.guild-id");
        if (guildId == null || guildId.equals("SUNUCU_ID_BURAYA")) {
            getLogger().warning("Discord sunucu ID'si ayarlanmamış! Lütfen config.yml dosyasını düzenleyin.");
            return;
        }
        
        String channelId = config.getString("discord.link-channel-id");
        if (channelId == null || channelId.equals("KANAL_ID_BURAYA")) {
            getLogger().warning("Discord kanal ID'si ayarlanmamış! Lütfen config.yml dosyasını düzenleyin.");
            return;
        }
        
        discordBot = new DiscordBot(this, token, guildId, channelId);
        discordBot.start();
    }
    
    public void reloadPlugin() {
        reloadConfig();
        config = getConfig();
        
        if (discordBot != null) {
            discordBot.shutdown();
        }
        
        setupDiscordBot();
    }
    
    public void sendMessage(CommandSender sender, String message) {
        String prefix = ChatColor.translateAlternateColorCodes('&', 
                config.getString("messages.prefix", "&8[&bDiscordLinker&8] &7"));
        sender.sendMessage(prefix + ChatColor.translateAlternateColorCodes('&', message));
    }
    
    public static DiscordLinker getInstance() {
        return instance;
    }
    
    public DiscordBot getDiscordBot() {
        return discordBot;
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public LinkManager getLinkManager() {
        return linkManager;
    }
    

}