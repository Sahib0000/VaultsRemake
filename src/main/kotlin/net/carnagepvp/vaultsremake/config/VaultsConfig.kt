package net.carnagepvp.vaultsremake.config

import net.carnagepvp.vaultsremake.core.config.ConfigurationBase
import net.carnagepvp.vaultsremake.core.config.annotations.ConfigField

object VaultsConfig : ConfigurationBase("config.yml") {

    @ConfigField(path = "mongo.address", comment = ["The address of the MongoDB server"])
    var mongoAddress = "localhost"

    @ConfigField(path = "mongo.port", comment = ["The port of the MongoDB server"])
    var mongoPort = 27017

    @ConfigField(path = "mongo.database", comment = ["The database to use"])
    var mongoDatabase = "vaults_remake"

    @ConfigField(path = "mongo.username", comment = ["The username for MongoDB authentication"])
    var mongoUsername = ""

    @ConfigField(path = "mongo.password", comment = ["The password for MongoDB authentication"])
    var mongoPassword = ""

    @ConfigField(path = "mongo.auth-database", comment = ["The authentication database to use"])
    var mongoAuthDatabase = "vaults_remake"

    @ConfigField(path = "default-max-vaults", comment = ["The default maximum number of vaults a player can have"])
    var defaultMaxVaults = 1

    @ConfigField(path = "blacklisted-items", comment = ["The list of items that are not allowed in vaults"])
    var blacklistedItems = mutableListOf("BEDROCK", "BARRIER")

    @ConfigField(path = "permissions.admin", comment = ["The permission for administrative actions"])
    var adminPermission = "vaultsremake.admin"

    @ConfigField(path = "messages.prefix", comment = ["The prefix for all plugin messages"])
    var prefix = "&b&lVaults"

    @ConfigField(path = "messages.no-permission", comment = ["Message sent when a player doesn't have permission"])
    var noPermission = "&cYou do not have permission to do this."

    @ConfigField(path = "messages.database-not-connected", comment = ["Message sent when the database is not connected"])
    var databaseNotConnected = "&cThe database is not currently connected. Please try again later."

    @ConfigField(path = "messages.vault-load-failed", comment = ["Message sent when vaults fail to load"])
    var vaultLoadFailed = "&cFailed to load your vaults. Please contact an administrator."

    @ConfigField(path = "messages.vault-load-error", comment = ["Message sent when an error occurs during vault loading"])
    var vaultLoadError = "&cAn error occurred while loading your vaults. Please contact an administrator."

    @ConfigField(path = "messages.invalid-vault-number", comment = ["Message sent when an invalid vault number is provided"])
    var invalidVaultNumber = "&cInvalid vault number."

    @ConfigField(path = "messages.player-not-found", comment = ["Message sent when a target player is not found"])
    var playerNotFound = "&cPlayer '{0}' not found."

    @ConfigField(path = "messages.no-permission-other", comment = ["Message sent when a player tries to open another player's vault without permission"])
    var noPermissionOther = "&cYou do not have permission to open other players vaults."

    @ConfigField(path = "messages.searching", comment = ["Message sent when searching vaults"])
    var searching = "&7Searching your vaults for &f{0}&7..."

    @ConfigField(path = "messages.searching-other", comment = ["Message sent when searching another player's vaults"])
    var searchingOther = "&7Searching &f{0}'s &7vaults for &f{1}&7..."

    @ConfigField(path = "messages.usage-format", comment = ["The format for usage messages"])
    var usageFormat = "&bUsage: &f{0}"

    @ConfigField(path = "messages.loading-vault", comment = ["Message sent when loading a vault"])
    var loadingVault = "&7Loading vault #{0}..."

    @ConfigField(path = "messages.loading-vault-other", comment = ["Message sent when loading another player's vault"])
    var loadingVaultOther = "&7Loading vault #{0} for {1}..."

    @ConfigField(path = "messages.reverting-vault", comment = ["Message sent when reverting a vault"])
    var revertingVault = "&7Reverting vault #{0} for &f{1}&7..."

    @ConfigField(path = "messages.revert-success", comment = ["Message sent when a vault is successfully reverted"])
    var revertSuccess = "&aSuccessfully reverted vault #{0} for &f{1}&a."

    @ConfigField(path = "messages.deleting-vault", comment = ["Message sent when deleting a vault"])
    var deletingVault = "&7Deleting vault #{0} for &f{1}&7..."

    @ConfigField(path = "messages.delete-success", comment = ["Message sent when a vault is successfully deleted"])
    var deleteSuccess = "&aSuccessfully deleted vault #{0} for &f{1}&a."

    @ConfigField(path = "messages.deleting-all-vaults", comment = ["Message sent when deleting all vaults for a player"])
    var deletingAllVaults = "&7Deleting all vaults for &f{0}&7..."

    @ConfigField(path = "messages.delete-all-success", comment = ["Message sent when all vaults for a player are successfully deleted"])
    var deleteAllSuccess = "&aSuccessfully deleted all vaults for &f{0}&a."

    @ConfigField(path = "messages.purging-database", comment = ["Message sent when purging the database"])
    var purgingDatabase = "&c&lWARNING! &7Purging the full database... Run the command again to confirm."

    @ConfigField(path = "messages.purge-success", comment = ["Message sent when the database is successfully purged"])
    var purgeSuccess = "&aSuccessfully purged the full database."

    @ConfigField(path = "messages.not-authorized", comment = ["Message sent when a player is not authorized (e.g. not OP)"])
    var notAuthorized = "&cYou are not authorized to use this command."

    @ConfigField(path = "messages.invalid-view-token", comment = ["Message sent when a view token is invalid or expired"])
    var invalidViewToken = "&cInvalid or expired vault view token."

    @ConfigField(path = "messages.no-permission-vault", comment = ["Message sent when a player doesn't have permission to access a specific vault number"])
    var noPermissionVault = "&cYou do not have permission to access vault #{0}."

    @ConfigField(path = "messages.deleting-vault-warning", comment = ["Warning message before deleting a vault"])
    var deletingVaultWarning = "&c&lWARNING! &7You are about to delete vault #{0} for &f{1}&7. Run the command again to confirm."

    @ConfigField(path = "messages.deleting-all-vaults-warning", comment = ["Warning message before deleting all vaults for a player"])
    var deletingAllVaultsWarning = "&c&lWARNING! &7You are about to delete ALL vaults for &f{0}&7. Run the command again to confirm."

    @ConfigField(path = "messages.fetching-backups", comment = ["Message sent when fetching backups"])
    var fetchingBackups = "&7Fetching backup history for &f{0} &7vault #{1}..."

    @ConfigField(path = "messages.no-backups-found", comment = ["Message sent when no backups are found"])
    var noBackupsFound = "&cNo backups found for &f{0} &cvault #{1}."

    @ConfigField(path = "messages.invalid-view-token-expired", comment = ["Message sent when a view token is invalid or expired"])
    var invalidViewTokenExpired = "&cInvalid or expired vault view token."

    @ConfigField(path = "messages.view-only-link", comment = ["Message sent when /pv view is used directly"])
    var viewOnlyLink = "&cThis command can only be used by clicking a vault link in chat."

    @ConfigField(path = "messages.gui-title", comment = ["The title for the vault GUI"])
    var guiTitle = "&b&lVault &f#{0}"

    @ConfigField(path = "messages.gui-title-read-only", comment = ["The title for the vault GUI in read-only mode"])
    var guiTitleReadOnly = "&b&lVault &f#{0} &7(Read-Only)"

    @ConfigField(path = "messages.blacklisted-item", comment = ["Message sent when trying to put a blacklisted item in a vault"])
    var blacklistedItem = "&cYou cannot put &f{0} &cin the vault!"

    @ConfigField(path = "messages.search-no-results", comment = ["Message sent when no search results are found"])
    var searchNoResults = "&cNo items found matching '{0}' in {1} vaults."

    @ConfigField(path = "messages.snapshot-load-failed", comment = ["Message sent when a snapshot fails to load"])
    var snapshotLoadFailed = "&cFailed to load snapshot data."

    @ConfigField(path = "messages.snapshot-restore-success", comment = ["Message sent when a snapshot is successfully restored"])
    var snapshotRestoreSuccess = "&aSuccessfully restored vault #{0} for &f{1} &afrom snapshot: &f{2}&a."

    @ConfigField(path = "messages.vault-icon-updated", comment = ["Message sent when a vault icon is updated"])
    var vaultIconUpdated = "&aVault #{0} icon updated!"

    @ConfigField(path = "messages.selector-title", comment = ["The title for the vault selector GUI"])
    var selectorTitle = "&b&lYour Vaults"

    @ConfigField(path = "messages.vault-item-name", comment = ["The display name for a vault item in the selector GUI"])
    var vaultItemName = "&bVault #{0}"

    @ConfigField(path = "messages.search-title", comment = ["The title for the search results GUI"])
    var searchTitle = "&b&lSearch Results: &f{0}"

    @ConfigField(path = "messages.backup-history-title", comment = ["The title for the backup history GUI"])
    var backupHistoryTitle = "&b&lBackup History: &f{0} #{1}"

    @ConfigField(path = "messages.material-selector-title", comment = ["The title for the material selector GUI"])
    var materialSelectorTitle = "&b&lPV Material Selector"
}
