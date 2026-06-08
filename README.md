# VaultsRemake

VaultsRemake is a custom player vault plugin for the upcoming release of Carnage. Designed from the ground up for maximum efficiency and reliability, it provides a seamless storage experience for players while offering powerful management tools for administrators.

## 🚀 Features

- **Standalone:** Zero dependencies on external "Core" plugins. All utilities are bundled within.
- **Asynchronous Data Handling:** Uses a custom Promise system and MongoDB for non-blocking data operations.
- **Dynamic GUIs:** Interactive menus for vault selection, searching, and administrative management.
- **Backup & Restore:** Full history of vault snapshots with the ability to revert to previous states.
- **Search System:** Search across all your vaults for specific items.
- **Highly Configurable:** Customize all messages, permissions, and database settings.
- **Item Blacklisting:** Prevent specific materials from being stored in vaults.
- **Admin Tools:** Extensive commands for managing player vaults, including view, delete, revert, and purge.

## 🛠 Installation

1. Download the `VaultsRemake.jar`.
2. Place it in your server's `plugins` folder.
3. Restart your server to generate the default configuration.
4. Configure your MongoDB connection in `plugins/VaultsRemake/config.yml`.
5. Restart or reload the plugin.

## ⚙️ Configuration

The `config.yml` file allows you to customize every aspect of the plugin:

- **MongoDB Settings:** Connection details for your database.
- **Permissions:** Custom permission nodes for admin actions and vault limits.
- **Messages:** All user-facing strings, supporting color codes and dynamic placeholders.
- **Vault Limits:** Set default maximum vaults per player.
- **Blacklist:** List of materials that cannot be stored.

## 📜 Commands & Permissions

### Player Commands
- `/pv [number]` - Open your vault (e.g., `/pv 1`).
- `/pv search <item>` - Search for an item across all your vaults.

### Admin Commands
- `/pv <player> <number>` - Open another player's vault.
- `/pv delete <player> <number>` - Delete a specific vault for a player.
- `/pv revert <player> <number>` - View and restore from vault backups.
- `/pv deleteall <player>` - Delete all vaults belonging to a player.
- `/pv purge` - Wipe the entire vaults database (requires confirmation).

### Permissions
- `vaultsremake.admin` - Access to all administrative commands (Configurable).
- `vaultsremake.amount.<number>` - Permission to have a specific number of vaults.

## 🏗 Building from Source

To build VaultsRemake, you will need Maven and a Java 8+ development environment.

```bash
mvn clean package
```

The compiled JAR will be located in the `target` directory.
