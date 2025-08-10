# DiscordLinker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

DiscordLinker, Minecraft sunucunuzdaki oyuncuların Discord hesaplarını kolayca eşlemelerine olanak tanıyan bir Spigot/Paper eklentisidir. Bu eklenti sayesinde oyuncular, sunucunuzdaki hesaplarını Discord hesaplarıyla bağlayabilir ve çeşitli entegrasyon avantajlarından yararlanabilirler.

## Özellikler

- **Kolay Hesap Eşleme**: Oyuncular basit bir komutla hesaplarını Discord'a bağlayabilirler
- **Discord Entegrasyonu**: Discord sunucunuzda özel bir eşleme kanalı ve mesajı oluşturur
- **Yönetici Komutları**: Hesap eşlemelerini kontrol etme ve yönetme imkanı
- **Veritabanı Desteği**: SQLite ve MySQL desteği ile verilerinizi güvenle saklayın
- **Özelleştirilebilir Mesajlar**: Tüm mesajları config.yml üzerinden özelleştirebilirsiniz

## Kurulum

1. Eklentiyi sunucunuzun `plugins` klasörüne yükleyin
2. Sunucuyu başlatın ve durdurun (config.yml dosyası oluşturulacaktır)
3. `plugins/DiscordLinker/config.yml` dosyasını düzenleyin:
   - Discord bot token'ınızı girin
   - Discord sunucu ID'nizi girin
   - Hesap eşleme kanalı ID'sini girin
4. Sunucuyu yeniden başlatın

## Discord Bot Kurulumu

1. [Discord Developer Portal](https://discord.com/developers/applications)'a gidin
2. "New Application" butonuna tıklayın
3. Botunuza bir isim verin ve oluşturun
4. Sol menüden "Bot" sekmesine gidin
5. "Add Bot" butonuna tıklayın
6. "Reset Token" butonuna tıklayarak token'ı alın
7. "MESSAGE CONTENT INTENT" seçeneğini aktif edin
8. OAuth2 > URL Generator bölümünden aşağıdaki izinleri seçin:
   - Scopes: bot
   - Bot Permissions: Send Messages, Embed Links, Read Message History, Use Slash Commands
9. Oluşturulan URL'yi kullanarak botu Discord sunucunuza ekleyin

## Komutlar

### Oyuncu Komutları
- `/esle` - Discord hesabınızı eşlemek için bir kod oluşturur

### Yönetici Komutları
- `/esleadmin unlink <oyuncu>` - Belirtilen oyuncunun hesap eşlemesini kaldırır
- `/esleadmin check <oyuncu>` - Belirtilen oyuncunun hesap eşlemesini kontrol eder
- `/esleadmin reload` - Eklenti yapılandırmasını yeniden yükler

## İzinler

- `discordlinker.link` - `/esle` komutunu kullanma izni
- `discordlinker.admin` - Yönetici komutlarını kullanma izni

## Yapılandırma

```yaml
# Discord Bot Ayarları
discord:
  token: "BOT_TOKEN_BURAYA"
  guild-id: "SUNUCU_ID_BURAYA"
  link-channel-id: "KANAL_ID_BURAYA"
  link-message-id: ""

# Veritabanı Ayarları
database:
  type: "sqlite"  # sqlite veya mysql
  sqlite-file: "discordlinker.db"
  mysql:
    host: "localhost"
    port: 3306
    database: "discordlinker"
    username: "root"
    password: "password"

# Mesaj Ayarları
messages:
  prefix: "&8[&bDiscordLinker&8] &7"
  code-generated: "&aDiscord hesabınızı eşlemek için kod oluşturuldu: &e%code%&a."
  link-success: "&aHesabınız başarıyla Discord hesabınızla eşlendi!"
  link-already: "&cHesabınız zaten bir Discord hesabıyla eşlenmiş!"

# Genel Ayarlar
settings:
  code-expiry: 300  # Saniye cinsinden
  code-length: 5
```

## Katkıda Bulunma

Bu projeye katkıda bulunmak isterseniz:

1. Bu depoyu fork edin
2. Yeni bir branch oluşturun (`git checkout -b feature/amazing-feature`)
3. Değişikliklerinizi commit edin (`git commit -m 'Add some amazing feature'`)
4. Branch'inize push edin (`git push origin feature/amazing-feature`)
5. Pull Request açın

## Lisans

Bu proje MIT Lisansı altında lisanslanmıştır. Daha fazla bilgi için [LICENSE](LICENSE) dosyasına bakın.

## İletişim

Sorularınız veya önerileriniz için GitHub üzerinden issue açabilirsiniz.