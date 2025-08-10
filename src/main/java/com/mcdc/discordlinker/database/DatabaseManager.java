package com.mcdc.discordlinker.database;

import com.mcdc.discordlinker.DiscordLinker;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final DiscordLinker plugin;
    private final String dbType;
    private Connection connection;
    
    // SQLite için
    private String sqliteFile;
    
    // MySQL için
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    
    /**
     * SQLite için constructor
     * 
     * @param plugin Plugin instance'ı
     * @param dbType Veritabanı tipi (sqlite)
     * @param sqliteFile SQLite dosya yolu
     */
    public DatabaseManager(DiscordLinker plugin, String dbType, String sqliteFile) {
        this.plugin = plugin;
        this.dbType = dbType.toLowerCase();
        this.sqliteFile = sqliteFile;
    }
    
    /**
     * MySQL için constructor
     * 
     * @param plugin Plugin instance'ı
     * @param dbType Veritabanı tipi (mysql)
     * @param host MySQL sunucu adresi
     * @param port MySQL sunucu portu
     * @param database Veritabanı adı
     * @param username Kullanıcı adı
     * @param password Şifre
     */
    public DatabaseManager(DiscordLinker plugin, String dbType, String host, int port, String database, String username, String password) {
        this.plugin = plugin;
        this.dbType = dbType.toLowerCase();
        this.host = host;
        this.port = port;
        this.database = database;
        this.username = username;
        this.password = password;
    }
    
    public void initialize() {
        try {
            connect();
            createTables();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Veritabanı bağlantısı kurulamadı!", e);
        }
    }
    
    private void connect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }
        
        if (dbType.equals("sqlite")) {
            // SQLite JDBC sürücüsünü yükle
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "SQLite JDBC sürücüsü bulunamadı!", e);
                return;
            }
            
            connection = DriverManager.getConnection("jdbc:sqlite:" + sqliteFile);
            plugin.getLogger().info("SQLite veritabanına bağlanıldı: " + sqliteFile);
        } else if (dbType.equals("mysql")) {
            // MySQL JDBC sürücüsünü yükle
            try {
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE, "MySQL JDBC sürücüsü bulunamadı!", e);
                return;
            }
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("MySQL veritabanına bağlanıldı: " + url);
        }
    }
    
    private void createTables() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        
        try (Statement statement = connection.createStatement()) {
            // mcdc tablosu (Minecraft -> Discord)
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS mcdc (" +
                "mc_uuid VARCHAR(36) PRIMARY KEY, " +
                "mc_username VARCHAR(16) NOT NULL, " +
                "discord_id VARCHAR(20) NOT NULL, " +
                "linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );
            
            // dcmc tablosu (Discord -> Minecraft)
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS dcmc (" +
                "discord_id VARCHAR(20) PRIMARY KEY, " +
                "mc_uuid VARCHAR(36) NOT NULL, " +
                "mc_username VARCHAR(16) NOT NULL, " +
                "linked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
            );
            
            // link_codes tablosu (Geçici eşleme kodları)
            statement.executeUpdate(
                "CREATE TABLE IF NOT EXISTS link_codes (" +
                "code VARCHAR(10) PRIMARY KEY, " +
                "mc_uuid VARCHAR(36) NOT NULL, " +
                "mc_username VARCHAR(16) NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "expires_at TIMESTAMP NOT NULL" +
                ");"
            );
            
            plugin.getLogger().info("Veritabanı tabloları başarıyla oluşturuldu.");
        }
    }
    
    public String getDiscordIdByUUID(UUID uuid) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "SELECT discord_id FROM mcdc WHERE mc_uuid = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("discord_id");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Discord ID getirme hatası!", e);
        }
        
        return null;
    }
    
    public UUID getUUIDByDiscordId(String discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "SELECT mc_uuid FROM dcmc WHERE discord_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, discordId);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return UUID.fromString(resultSet.getString("mc_uuid"));
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "UUID getirme hatası!", e);
        }
        
        return null;
    }
    
    public String getUsernameByDiscordId(String discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "SELECT mc_username FROM dcmc WHERE discord_id = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, discordId);
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        return resultSet.getString("mc_username");
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Kullanıcı adı getirme hatası!", e);
        }
        
        return null;
    }
    
    public boolean linkAccounts(UUID uuid, String username, String discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            connection.setAutoCommit(false);
            
            try {
                // mcdc tablosuna ekle
                String sqlMcDc = "INSERT OR REPLACE INTO mcdc (mc_uuid, mc_username, discord_id) VALUES (?, ?, ?)";
                if (dbType.equals("mysql")) {
                    sqlMcDc = "INSERT INTO mcdc (mc_uuid, mc_username, discord_id) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE mc_username = ?, discord_id = ?";
                }
                
                try (PreparedStatement statement = connection.prepareStatement(sqlMcDc)) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, username);
                    statement.setString(3, discordId);
                    
                    if (dbType.equals("mysql")) {
                        statement.setString(4, username);
                        statement.setString(5, discordId);
                    }
                    
                    statement.executeUpdate();
                }
                
                // dcmc tablosuna ekle
                String sqlDcMc = "INSERT OR REPLACE INTO dcmc (discord_id, mc_uuid, mc_username) VALUES (?, ?, ?)";
                if (dbType.equals("mysql")) {
                    sqlDcMc = "INSERT INTO dcmc (discord_id, mc_uuid, mc_username) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE mc_uuid = ?, mc_username = ?";
                }
                
                try (PreparedStatement statement = connection.prepareStatement(sqlDcMc)) {
                    statement.setString(1, discordId);
                    statement.setString(2, uuid.toString());
                    statement.setString(3, username);
                    
                    if (dbType.equals("mysql")) {
                        statement.setString(4, uuid.toString());
                        statement.setString(5, username);
                    }
                    
                    statement.executeUpdate();
                }
                
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Hesap eşleme hatası!", e);
            return false;
        }
    }
    
    public boolean saveCode(UUID uuid, String username, String code, long expiresAt) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "INSERT OR REPLACE INTO link_codes (code, mc_uuid, mc_username, expires_at) VALUES (?, ?, ?, ?)";
            if (dbType.equals("mysql")) {
                sql = "INSERT INTO link_codes (code, mc_uuid, mc_username, expires_at) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE mc_uuid = ?, mc_username = ?, expires_at = ?";
            }
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, code);
                statement.setString(2, uuid.toString());
                statement.setString(3, username);
                statement.setTimestamp(4, new Timestamp(expiresAt));
                
                if (dbType.equals("mysql")) {
                    statement.setString(5, uuid.toString());
                    statement.setString(6, username);
                    statement.setTimestamp(7, new Timestamp(expiresAt));
                }
                
                statement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Kod kaydetme hatası!", e);
            return false;
        }
    }
    
    @Deprecated
    public boolean saveCode(String code, UUID uuid, String username, Timestamp expiresAt) {
        return saveCode(uuid, username, code, expiresAt.getTime());
    }
    
    public String[] validateCode(String code) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "SELECT mc_uuid, mc_username FROM link_codes WHERE code = ? AND expires_at > ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, code);
                statement.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        String uuid = resultSet.getString("mc_uuid");
                        String username = resultSet.getString("mc_username");
                        
                        // Kodu sil
                        deleteCode(code);
                        
                        return new String[]{uuid, username};
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Kod doğrulama hatası!", e);
        }
        
        return null;
    }
    
    public void deleteCode(String code) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "DELETE FROM link_codes WHERE code = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, code);
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Kod silme hatası!", e);
        }
    }
    
    public void cleanupExpiredCodes() {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            String sql = "DELETE FROM link_codes WHERE expires_at <= ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                int count = statement.executeUpdate();
                
                if (count > 0) {
                    plugin.getLogger().info(count + " adet süresi dolmuş kod temizlendi.");
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Kod temizleme hatası!", e);
        }
    }
    
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("Veritabanı bağlantısı kapatıldı.");
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Veritabanı bağlantısı kapatılırken hata oluştu!", e);
        }
    }
    
    public boolean unlinkAccounts(UUID uuid, String discordId) {
        try {
            if (connection == null || connection.isClosed()) {
                connect();
            }
            
            connection.setAutoCommit(false);
            
            try {
                // mcdc tablosundan sil
                String sqlMcDc = "DELETE FROM mcdc WHERE mc_uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(sqlMcDc)) {
                    statement.setString(1, uuid.toString());
                    statement.executeUpdate();
                }
                
                // dcmc tablosundan sil
                String sqlDcMc = "DELETE FROM dcmc WHERE discord_id = ?";
                try (PreparedStatement statement = connection.prepareStatement(sqlDcMc)) {
                    statement.setString(1, discordId);
                    statement.executeUpdate();
                }
                
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Hesap eşlemesi kaldırma hatası!", e);
            return false;
        }
    }
}