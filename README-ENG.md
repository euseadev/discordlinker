# DiscordLinker

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

DiscordLinker is a Spigot/Paper plugin that allows players on your Minecraft server to easily link their Discord accounts. With this plugin, players can connect their in-game accounts to their Discord accounts and take advantage of various integration features.

## Features

* **Easy Account Linking**: Players can link their accounts to Discord with a simple command
* **Discord Integration**: Creates a dedicated linking channel and message in your Discord server
* **Admin Commands**: Check and manage account linkings
* **Database Support**: Store your data securely with SQLite or MySQL support
* **Customizable Messages**: All messages can be customized via `config.yml`

## Installation

1. Upload the plugin to your server's `plugins` folder
2. Start and stop the server (this will generate the `config.yml` file)
3. Edit `plugins/DiscordLinker/config.yml`:

   * Enter your Discord bot token
   * Enter your Discord server (guild) ID
   * Enter the link channel ID
4. Restart the server

## Discord Bot Setup

1. Go to [Discord Developer Portal](https://discord.com/developers/applications)
2. Click the "New Application" button
3. Give your bot a name and create it
4. From the left menu, go to the "Bot" section
5. Click the "Add Bot" button
6. Click "Reset Token" to obtain your bot token
7. Enable the "MESSAGE CONTENT INTENT" option
8. Under OAuth2 > URL Generator, select the following:

   * Scopes: bot
   * Bot Permissions: Send Messages, Embed Links, Read Message History, Use Slash Commands
9. Use the generated URL to add the bot to your Discord server

## Commands

### Player Commands

* `/esle` – Generates a code to link your Discord account

### Admin Commands

* `/esleadmin unlink <player>` – Removes the specified player's account linking
* `/esleadmin check <player>` – Checks the specified player's account linking
* `/esleadmin reload` – Reloads the plugin configuration

## Permissions

* `discordlinker.link` – Permission to use the `/esle` command
* `discordlinker.admin` – Permission to use admin commands

## Configuration

```yaml
# Discord Bot Settings
discord:
  token: "BOT_TOKEN_HERE"
  guild-id: "GUILD_ID_HERE"
  link-channel-id: "CHANNEL_ID_HERE"
  link-message-id: ""

# Database Settings
database:
  type: "sqlite"  # sqlite or mysql
  sqlite-file: "discordlinker.db"
  mysql:
    host: "localhost"
    port: 3306
    database: "discordlinker"
    username: "root"
    password: "password"

# Message Settings
messages:
  prefix: "&8[&bDiscordLinker&8] &7"
  code-generated: "&aA code has been generated to link your Discord account: &e%code%&a."
  link-success: "&aYour account has been successfully linked to your Discord account!"
  link-already: "&cYour account is already linked to a Discord account!"

# General Settings
settings:
  code-expiry: 300  # In seconds
  code-length: 5
```

## Contributing

If you’d like to contribute to this project:

1. Fork this repository
2. Create a new branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to your branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License. For more details, see the [LICENSE](LICENSE) file.

## Contact

For questions or suggestions, you can open an issue on GitHub.

