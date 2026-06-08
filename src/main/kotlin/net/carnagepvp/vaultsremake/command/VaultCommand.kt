package net.carnagepvp.vaultsremake.command

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.gui.VaultBackupHistoryGui
import net.carnagepvp.vaultsremake.gui.VaultGui
import net.carnagepvp.vaultsremake.gui.VaultSearchGui
import net.carnagepvp.vaultsremake.gui.VaultSelectorGui
import net.carnagepvp.vaultsremake.struct.VaultManager
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player

@CommandAlias("vaultremake|pvremake")
class VaultCommand(private val vaultManager: VaultManager) : BaseCommand() {

    private val pendingConfirmations = mutableMapOf<String, String>()

    @Default
    fun onVault(player: Player, @Optional args: Array<String>?) {
        if (!vaultManager.isConnected()) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.databaseNotConnected))
            return
        }

        if (args.isNullOrEmpty()) {
            vaultManager.getAllVaults(player).thenAcceptSync { allVaults ->
                if (allVaults.isEmpty()) {
                    player.sendMessage(VaultsPlugin.prefix(VaultsConfig.vaultLoadFailed))
                    return@thenAcceptSync
                }
                VaultSelectorGui(player, vaultManager, allVaults).open()
            }.exceptionally { ex ->
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.vaultLoadError))
                VaultsPlugin.instance.logger.severe("Failed to load vaults for ${player.uniqueId}: ${ex.message}")
                null
            }
            return
        }

        if (args.size == 1) {
            val vaultNumber = args[0].toIntOrNull()
            if (vaultNumber == null) {
                sendUsage(player)
                return
            }

            openVault(player, player, vaultNumber, isAdmin = false)
            return
        }

        if (args.size == 2) {
            if (!player.hasPermission(VaultsConfig.adminPermission)) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.noPermissionOther))
                return
            }

            val targetName = args[0]
            val vaultNumber = args[1].toIntOrNull()

            if (vaultNumber == null || vaultNumber <= 0) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.invalidVaultNumber))
                return
            }

            val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
            if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.playerNotFound, targetName))
                return
            }

            openVault(player, target, vaultNumber, isAdmin = true)
            return
        }

        sendUsage(player)
    }

    @Subcommand("search")
    fun onSearch(player: Player, args: Array<String>?) {
        if (!vaultManager.isConnected()) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.databaseNotConnected))
            return
        }

        if (args.isNullOrEmpty()) {
            sendUsage(player)
            return
        }

        if (args.size == 1) {
            val query = args[0]
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.searching, query))

            vaultManager.getAllVaults(player).thenAcceptSync { allVaults ->
                VaultSearchGui(player, player, query, vaultManager, allVaults).open()
            }
            return
        }

        if (args.size >= 2) {
            if (!player.hasPermission(VaultsConfig.adminPermission)) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.noPermissionOther))
                return
            }

            val targetName = args[0]
            val query = args.sliceArray(1 until args.size).joinToString(" ")

            val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
            if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.playerNotFound, targetName))
                return
            }

            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.searchingOther, target.name ?: targetName, query))

            vaultManager.getAllVaults(target.uniqueId, 99).thenAcceptSync { allVaults ->
                VaultSearchGui(player, target, query, vaultManager, allVaults).open()
            }
            return
        }
    }

    @Subcommand("delete")
    fun onDelete(player: Player, args: Array<String>) {
        if (!player.hasPermission(VaultsConfig.adminPermission)) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.noPermission))
            return
        }
        if (args.size < 2) {
            player.sendMessage(CC.translate(VaultsConfig.usageFormat.replace("{0}", "/pv delete <player> <vault #>")))
            return
        }

        val targetName = args[0]
        val vaultNumber = args[1].toIntOrNull()

        if (vaultNumber == null || vaultNumber <= 0) {
            player.sendMessage(VaultsPlugin.prefix("&cInvalid vault number."))
            return
        }

        val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
        
        if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
            player.sendMessage(VaultsPlugin.prefix("&cPlayer '$targetName' not found."))
            return
        }

        val confirmationKey = "delete:${target.uniqueId}:$vaultNumber"
        if (pendingConfirmations[player.name] != confirmationKey) {
            pendingConfirmations[player.name] = confirmationKey
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deletingVaultWarning, vaultNumber, target.name ?: targetName))
            Bukkit.getScheduler().runTaskLater(VaultsPlugin.instance, {
                if (pendingConfirmations[player.name] == confirmationKey) {
                    pendingConfirmations.remove(player.name)
                }
            }, 200L)
            return
        }

        pendingConfirmations.remove(player.name)
        player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deletingVault, vaultNumber, target.name ?: targetName))
        
        vaultManager.deleteVault(target.uniqueId, vaultNumber).thenAcceptSync {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deleteSuccess, vaultNumber, target.name ?: targetName))
        }
    }

    @Subcommand("revert")
    fun onRevert(player: Player, args: Array<String>) {
        if (!player.hasPermission(VaultsConfig.adminPermission)) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.noPermission))
            return
        }
        if (args.size < 2) {
            player.sendMessage(CC.translate(VaultsConfig.usageFormat.replace("{0}", "/pv revert <player> <vault #>")))
            return
        }

        val targetName = args[0]
        val vaultNumber = args[1].toIntOrNull()

        if (vaultNumber == null || vaultNumber <= 0) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.invalidVaultNumber))
            return
        }

        val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
        
        if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.playerNotFound, targetName))
            return
        }

        player.sendMessage(VaultsPlugin.prefix(VaultsConfig.fetchingBackups, target.name ?: targetName, vaultNumber))

        vaultManager.getBackups(target.uniqueId, vaultNumber).thenAcceptSync { backups ->
            if (backups.isEmpty()) {
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.noBackupsFound, target.name ?: targetName, vaultNumber))
                return@thenAcceptSync
            }

            VaultBackupHistoryGui(player, target, vaultNumber, vaultManager, backups).open()
        }
    }

    @Subcommand("deleteall")
    fun onDeleteAll(player: Player, args: Array<String>) {
        if (!player.isOp) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.notAuthorized))
            return
        }

        if (args.isEmpty()) {
            player.sendMessage(CC.translate(VaultsConfig.usageFormat.replace("{0}", "/pv deleteall <player>")))
            return
        }

        val targetName = args[0]
        val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
        
        if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.playerNotFound, targetName))
            return
        }

        val confirmationKey = "deleteall:${target.uniqueId}"
        if (pendingConfirmations[player.name] != confirmationKey) {
            pendingConfirmations[player.name] = confirmationKey
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deletingAllVaultsWarning, target.name ?: targetName))
            Bukkit.getScheduler().runTaskLater(VaultsPlugin.instance, {
                if (pendingConfirmations[player.name] == confirmationKey) {
                    pendingConfirmations.remove(player.name)
                }
            }, 200L)
            return
        }

        pendingConfirmations.remove(player.name)
        player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deletingAllVaults, target.name ?: targetName))
        
        vaultManager.deleteAllVaults(target.uniqueId).thenAcceptSync {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.deleteAllSuccess, target.name ?: targetName))
        }
    }

    @Subcommand("purge")
    fun onPurge(player: Player) {
        if (!player.isOp) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.notAuthorized))
            return
        }

        if (pendingConfirmations[player.name] != "purge") {
            pendingConfirmations[player.name] = "purge"
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.purgingDatabase))

            Bukkit.getScheduler().runTaskLater(VaultsPlugin.instance, {
                if (pendingConfirmations[player.name] == "purge") {
                    pendingConfirmations.remove(player.name)
                }
            }, 200L)
            return
        }

        pendingConfirmations.remove(player.name)
        vaultManager.purgeDatabase().thenAcceptSync {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.purgeSuccess))
        }
    }

    @Subcommand("view")
    fun onView(player: Player, args: Array<String>) {
        if (args.size < 3) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.viewOnlyLink))
            return
        }

        val targetName = args[0]
        val vaultNumber = args[1].toIntOrNull() ?: return
        val token = args[2]

        val target = Bukkit.getPlayer(targetName) ?: Bukkit.getOfflinePlayers().find { it.name != null && it.name.equals(targetName, ignoreCase = true) }
        if (target == null || (!target.isOnline && !target.hasPlayedBefore())) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.playerNotFound, targetName))
            return
        }

        if (!vaultManager.isValidViewToken(target.uniqueId, vaultNumber, token)) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.invalidViewTokenExpired))
            return
        }

        val compositeKey = "${target.uniqueId}:$vaultNumber"
        vaultManager.load(compositeKey).thenAcceptSync { data ->
            val vaultData = data ?: vaultManager.getOrCreateData(target.uniqueId, vaultNumber)
            VaultGui(player, vaultNumber, vaultData, vaultManager, readOnly = true).open()
        }
    }

    private fun openVault(viewer: Player, owner: OfflinePlayer, vaultNumber: Int, isAdmin: Boolean) {
        if (vaultNumber <= 0) {
            viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.invalidVaultNumber))
            return
        }

        if (!isAdmin) {
            val maxVaults = vaultManager.getMaxVaults(viewer)
            if (vaultNumber > maxVaults) {
                viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.noPermissionVault, vaultNumber))
                return
            }
        }

        if (isAdmin) {
            viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.loadingVaultOther, vaultNumber, owner.name ?: owner.uniqueId.toString()))
        } else {
            viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.loadingVault, vaultNumber))
        }

        val compositeKey = "${owner.uniqueId}:$vaultNumber"

        vaultManager.load(compositeKey).thenAcceptSync { data ->
            val vaultData = data ?: vaultManager.getOrCreateData(owner.uniqueId, vaultNumber)
            VaultGui(viewer, vaultNumber, vaultData, vaultManager).open()
        }.exceptionally { ex ->
            viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.vaultLoadError))
            VaultsPlugin.instance.logger.severe("Failed to load vault $vaultNumber for ${owner.uniqueId}: ${ex.message}")
            null
        }
    }

    private fun sendUsage(player: Player) {
        val usage = when {
            player.isOp -> listOf(
                VaultsConfig.usageFormat.replace("{0}", "/pv <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv search <query>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv <player> <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv search <player> <query>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv delete <player> <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv revert <player> <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv deleteall <player>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv purge")
            )
            player.hasPermission(VaultsConfig.adminPermission) -> listOf(
                VaultsConfig.usageFormat.replace("{0}", "/pv <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv search <query>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv <player> <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv search <player> <query>")
            )
            else -> listOf(
                VaultsConfig.usageFormat.replace("{0}", "/pv <vault #>"),
                VaultsConfig.usageFormat.replace("{0}", "/pv search <query>")
            )
        }
        usage.forEach { player.sendMessage(CC.translate(it)) }
    }
}