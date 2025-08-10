package com.mcdc.discordlinker.discord;

import com.mcdc.discordlinker.DiscordLinker;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.UUID;
import java.util.logging.Level;

public class DiscordBot extends ListenerAdapter {

    private final DiscordLinker plugin;
    private final String token;
    private final String guildId;
    private final String channelId;
    private JDA jda;
    private Guild guild;
    private TextChannel linkChannel;
    private Message linkMessage;
    
    /**
     * Discord bot constructor
     * 
     * @param plugin Plugin instance'ı
     * @param token Discord bot token'ı
     * @param guildId Discord sunucu ID'si
     * @param channelId Discord kanal ID'si
     */
    public DiscordBot(DiscordLinker plugin, String token, String guildId, String channelId) {
        this.plugin = plugin;
        this.token = token;
        this.guildId = guildId;
        this.channelId = channelId;
    }
    
    public void start() {
        try {
            // JDA'yı başlat
            jda = JDABuilder.createDefault(token, EnumSet.of(
                    GatewayIntent.GUILD_MESSAGES,
                    GatewayIntent.GUILD_MESSAGE_REACTIONS,
                    GatewayIntent.GUILD_MEMBERS
            ))
                    .addEventListeners(this)
                    .build();
            
            // Bağlantı kurulana kadar bekle
            jda.awaitReady();
            
            // Sadece bot adını kullanarak log oluştur
            User selfUser = jda.getSelfUser();
            String userName = selfUser.getName();
            plugin.getLogger().info("Discord botuna bağlanıldı: " + userName);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Discord botuna bağlanırken hata oluştu!", e);
        }
    }
    
    public void shutdown() {
        if (jda != null) {
            jda.shutdown();
            plugin.getLogger().info("Discord botu kapatıldı.");
        }
    }
    
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            // Sunucuyu bul
            guild = jda.getGuildById(guildId);
            if (guild == null) {
                plugin.getLogger().severe("Discord sunucusu bulunamadı! ID: " + guildId);
                return;
            }
            
            // Kanalı bul
            linkChannel = guild.getTextChannelById(channelId);
            if (linkChannel == null) {
                plugin.getLogger().severe("Discord kanalı bulunamadı! ID: " + channelId);
                return;
            }
            
            // Eşleme mesajını oluştur veya güncelle
            setupLinkMessage();
            
            plugin.getLogger().info("Discord bot hazır! Sunucu: " + guild.getName() + ", Kanal: " + linkChannel.getName());
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Discord bot hazırlanırken hata oluştu!", e);
        }
    }
    
    private void setupLinkMessage() {
        String messageId = plugin.getConfig().getString("discord.link-message-id", "");
        
        if (!messageId.isEmpty()) {
            try {
                // Mevcut mesajı bul
                linkChannel.retrieveMessageById(messageId).queue(
                    message -> {
                        linkMessage = message;
                        updateLinkMessage();
                    },
                    error -> {
                        plugin.getLogger().warning("Eşleme mesajı bulunamadı! Yeni mesaj oluşturuluyor...");
                        createLinkMessage();
                    }
                );
            } catch (Exception e) {
                plugin.getLogger().warning("Eşleme mesajı bulunamadı! Yeni mesaj oluşturuluyor...");
                createLinkMessage();
            }
        } else {
            createLinkMessage();
        }
    }
    
    private void createLinkMessage() {
        MessageCreateData message = new MessageCreateBuilder()
                .setContent(":link: **Minecraft Hesap Eşleme**\n\nMinecraft hesabınızı Discord hesabınızla eşlemek için aşağıdaki butona tıklayın ve Minecraft'ta aldığınız kodu girin.")
                .setActionRow(Button.primary("link_account", "Hesabımı Eşle"))
                .build();
        
        linkChannel.sendMessage(message).queue(sentMessage -> {
            linkMessage = sentMessage;
            
            // Mesaj ID'sini kaydet
            plugin.getConfig().set("discord.link-message-id", sentMessage.getId());
            plugin.saveConfig();
            
            plugin.getLogger().info("Eşleme mesajı oluşturuldu! ID: " + sentMessage.getId());
        });
    }
    
    private void updateLinkMessage() {
        if (linkMessage != null) {
            linkMessage.editMessage(":link: **Minecraft Hesap Eşleme**\n\nMinecraft hesabınızı Discord hesabınızla eşlemek için aşağıdaki butona tıklayın ve Minecraft'ta aldığınız kodu girin.")
                    .setActionRow(Button.primary("link_account", "Hesabımı Eşle"))
                    .queue();
            
            plugin.getLogger().info("Eşleme mesajı güncellendi! ID: " + linkMessage.getId());
        }
    }
    
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("link_account")) {
            // Kullanıcının zaten eşlenmiş olup olmadığını kontrol et
            String discordId = event.getUser().getId();
            UUID uuid = plugin.getDatabaseManager().getUUIDByDiscordId(discordId);
            
            if (uuid != null) {
                // Zaten eşlenmiş
                String username = plugin.getDatabaseManager().getUsernameByDiscordId(discordId);
                event.reply(":x: Hesabınız zaten **" + username + "** Minecraft hesabıyla eşlenmiş!")
                        .setEphemeral(true)
                        .queue();
                return;
            }
            
            // Kod giriş modalını göster
            TextInput codeInput = TextInput.create("link_code", "Eşleme Kodu", TextInputStyle.SHORT)
                    .setPlaceholder("Minecraft'ta aldığınız 5 haneli kodu girin")
                    .setMinLength(5)
                    .setMaxLength(5)
                    .setRequired(true)
                    .build();
            
            // addActionRows() yerine addComponents() kullanarak modal oluştur
            Modal modal = Modal.create("link_modal", "Minecraft Hesap Eşleme")
                    .addComponents(ActionRow.of(codeInput))
                    .build();
            
            event.replyModal(modal).queue();
        }
    }
    
    @Override
    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        if (event.getModalId().equals("link_modal")) {
            String code = event.getValue("link_code").getAsString();
            String discordId = event.getUser().getId();
            
            // Kodu doğrula
            String[] result = plugin.getDatabaseManager().validateCode(code);
            
            if (result != null) {
                String uuidStr = result[0];
                String username = result[1];
                UUID uuid = UUID.fromString(uuidStr);
                
                // Hesapları eşle
                boolean success = plugin.getDatabaseManager().linkAccounts(uuid, username, discordId);
                
                if (success) {
                    // Başarılı
                    event.reply(":white_check_mark: Hesabınız başarıyla **" + username + "** Minecraft hesabıyla eşlendi!")
                            .setEphemeral(true)
                            .queue();
                    
                    // Oyuncuya bildir (eğer çevrimiçiyse)
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            plugin.sendMessage(player, plugin.getConfig().getString("messages.link-success", "&aHesabınız başarıyla Discord hesabınızla eşlendi!"));
                        });
                    }
                    
                    plugin.getLogger().info(username + " (" + uuid + ") hesabı Discord ID: " + discordId + " ile eşlendi!");
                } else {
                    // Başarısız
                    event.reply(":x: Hesap eşleme sırasında bir hata oluştu! Lütfen tekrar deneyin.")
                            .setEphemeral(true)
                            .queue();
                }
            } else {
                // Geçersiz kod
                event.reply(":x: Geçersiz veya süresi dolmuş eşleme kodu! Lütfen Minecraft'ta `/esle` komutunu kullanarak yeni bir kod alın.")
                        .setEphemeral(true)
                        .queue();
            }
        }
    }
    
    public String generateLinkCode(Player player) {
        int codeLength = plugin.getConfig().getInt("settings.code-length", 5);
        String code = generateRandomCode(codeLength);
        
        long expiryTime = System.currentTimeMillis() + (plugin.getConfig().getInt("settings.code-expiry", 5) * 60 * 1000);
        plugin.getDatabaseManager().saveCode(player.getUniqueId(), player.getName(), code, expiryTime);
        
        return code;
    }
    
    private String generateRandomCode(int length) {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // Karışıklık yaratabilecek karakterler (0, 1, I, O) hariç
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        
        return sb.toString();
    }
    
    public JDA getJda() {
        return jda;
    }
}