package net.carnagepvp.vaultsremake.gui

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemBuilder
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.struct.VaultManager
import net.kyori.adventure.text.Component
import org.bson.Document
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import java.text.SimpleDateFormat
import java.util.*

class VaultBackupHistoryGui(
    private val admin: Player,
    private val target: OfflinePlayer,
    private val vaultNumber: Int,
    private val vaultManager: VaultManager,
    private val backups: List<Document>
) {

    private val gui: PaginatedGui = Gui.paginated()
        .title(Component.text(CC.translate(VaultsConfig.backupHistoryTitle.replace("{0}", target.name ?: "Unknown").replace("{1}", vaultNumber.toString()))))
        .rows(4)
        .pageSize(27)
        .disableAllInteractions()
        .create()

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss")

    fun open() {
        gui.clearPageItems()

        for (backup in backups) {
            val backupAt = backup.getLong("backup_at") ?: 0L
            val dateStr = dateFormat.format(Date(backupAt))

            val item = ItemBuilder(Material.PAPER)
                .name("&bSnapshot &7- &f$dateStr")
                .lore(
                    "",
                    "&b&lInformation",
                    "&b┆ &7Date: &f$dateStr",
                    "&b┆ &7Vault: &f#$vaultNumber",
                    "",
                    "&b&lActions",
                    "&b┆ &7Left-Click: &fView Contents",
                    "&b┆ &7Right-Click: &fRestore Snapshot",
                    "",
                    "&7(( Snapshot taken during deletion ))"
                )

            val guiItem = GuiItem(item) { event ->
                when (event.click) {
                    ClickType.LEFT -> {
                        val doc = Document(backup)
                        doc.remove("_id")
                        doc.remove("original_id")
                        doc.remove("backup_at")
                        
                        val data = vaultManager.deserialize(doc.toJson())
                        if (data != null) {
                            VaultGui(admin, vaultNumber, data, vaultManager, readOnly = true).open()
                        } else {
                            admin.sendMessage(VaultsPlugin.prefix(VaultsConfig.snapshotLoadFailed))
                        }
                    }
                    ClickType.RIGHT -> {
                        vaultManager.restoreVaultFromDoc(target.uniqueId, vaultNumber, backup)
                        admin.sendMessage(VaultsPlugin.prefix(VaultsConfig.snapshotRestoreSuccess, vaultNumber, target.name ?: target.uniqueId.toString(), dateStr))
                        admin.closeInventory()
                        admin.playSound(admin.location, Sound.LEVEL_UP, 1.0f, 1.0f)
                    }
                    else -> {}
                }
            }

            gui.addItem(guiItem)
        }

        gui.filler.fillBottom(GuiItem(ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name("&7")))
        gui.setItem(30, getPreviousButton())
        gui.setItem(32, getNextButton())

        gui.open(admin)
    }

    private fun getPreviousButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=")
            .name("&aPrevious Page").lore("", "&7(( Click me to go to the &f&oprevious &7page ))")
        return GuiItem(item) {
            if (gui.previous()) {
                admin.playSound(admin.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }

    private fun getNextButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=")
            .name("&aNext Page").lore("", "&7(( Click me to go to the &f&onext &7page ))")
        return GuiItem(item) {
            if (gui.next()) {
                admin.playSound(admin.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }
}
