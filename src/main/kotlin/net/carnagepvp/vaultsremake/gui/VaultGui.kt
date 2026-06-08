package net.carnagepvp.vaultsremake.gui

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.StorageGui
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemSerializer
import net.carnagepvp.vaultsremake.core.util.item.ItemUtils
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.data.VaultData
import net.carnagepvp.vaultsremake.data.VaultItem
import net.carnagepvp.vaultsremake.struct.VaultManager
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent

class VaultGui(
    private val player: Player,
    private val vaultNumber: Int,
    private val vaultData: VaultData,
    private val vaultManager: VaultManager,
    private val readOnly: Boolean = false
) {

    fun open() {
        val gui: StorageGui = Gui.storage()
            .title(Component.text(CC.translate(
                if (readOnly) VaultsConfig.guiTitleReadOnly.replace("{0}", vaultNumber.toString())
                else VaultsConfig.guiTitle.replace("{0}", vaultNumber.toString())
            )))
            .rows(6)
            .create()

        if (vaultData.items.isNotEmpty()) {
            for (itemData in vaultData.items) {
                val item = ItemSerializer.fromBase64(itemData.data).firstOrNull() ?: continue
                gui.inventory.setItem(itemData.slot, item)
            }
        }

        gui.setDefaultClickAction { event ->
            if (readOnly) {
                event.isCancelled = true
                return@setDefaultClickAction
            }

            if (handleBlacklist(event)) {
                event.isCancelled = true
            }
        }

        gui.setCloseGuiAction { _ ->
            if (readOnly) return@setCloseGuiAction

            val newItems = mutableListOf<VaultItem>()
            for (i in 0 until gui.inventory.size) {
                val item = gui.inventory.getItem(i)
                if (item == null || item.type == org.bukkit.Material.AIR) continue

                val base64 = ItemSerializer.toBase64(arrayOf(item))
                newItems.add(VaultItem(i, base64))
            }

            vaultData.items = newItems
            vaultManager.save(vaultData)
        }

        gui.open(player)
    }

    private fun handleBlacklist(event: InventoryClickEvent): Boolean {
        val item = when {
            event.isShiftClick && event.clickedInventory == event.view.bottomInventory -> event.currentItem
            event.clickedInventory == event.view.topInventory -> event.cursor
            else -> null
        }

        if (vaultManager.isBlacklisted(item)) {
            player.sendMessage(VaultsPlugin.prefix(VaultsConfig.blacklistedItem, ItemUtils.getDisplayName(item!!)))
            return true
        }
        return false
    }
}
