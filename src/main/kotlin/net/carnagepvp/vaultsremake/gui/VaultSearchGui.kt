package net.carnagepvp.vaultsremake.gui

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemBuilder
import net.carnagepvp.vaultsremake.core.util.item.ItemSerializer
import net.carnagepvp.vaultsremake.core.util.item.ItemUtils
import net.carnagepvp.vaultsremake.data.VaultData
import net.carnagepvp.vaultsremake.struct.VaultManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import net.carnagepvp.vaultsremake.VaultsPlugin
import org.bukkit.Sound

class VaultSearchGui(
    private val viewer: Player,
    private val owner: OfflinePlayer,
    private val query: String,
    private val vaultManager: VaultManager,
    private val allVaults: List<VaultData>
) {

    private val gui: PaginatedGui = Gui.paginated()
        .title(Component.text(CC.translate(VaultsConfig.searchTitle.replace("{0}", query))))
        .rows(6)
        .disableAllInteractions()
        .pageSize(45)
        .create()

    fun open() {
        var foundCount = 0
        allVaults.forEachIndexed { index, data ->
            val vaultNumber = index + 1
            if (data.items.isEmpty()) return@forEachIndexed

            val items = data.items.mapNotNull { itemData ->
                ItemSerializer.fromBase64(itemData.data).firstOrNull()
            }

            items.forEach { item ->
                if (matchesQuery(item, query)) {
                    val currentLore = item.itemMeta?.lore ?: mutableListOf<String>()
                    currentLore.add("")
                    currentLore.add(CC.translate("&7Left-Click to open vault &f#$vaultNumber&7."))

                    val searchItem = ItemBuilder(item.clone())
                        .name("&b${ItemUtils.getDisplayName(item)} &r&8- &bVault #$vaultNumber")
                        .clearLore()
                        .lore(currentLore)

                    val guiItem = GuiItem(searchItem) {
                        VaultGui(viewer, vaultNumber, data, vaultManager).open()
                    }

                    gui.addItem(guiItem)
                    foundCount++
                }
            }
        }

        if (foundCount == 0) {
            viewer.sendMessage(VaultsPlugin.prefix(VaultsConfig.searchNoResults, query, if (viewer == owner) "your" else "${owner.name}'s"))
            return
        }

        gui.filler.fillBottom(GuiItem(ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name("&7")))
        gui.setItem(48, getPreviousButton())
        gui.setItem(50, getNextButton())

        gui.open(viewer)
    }

    private fun matchesQuery(item: ItemStack, query: String): Boolean {
        val lowerQuery = CC.strip(query).lowercase()

        val displayName = CC.strip(ItemUtils.getDisplayName(item)).lowercase()
        if (displayName.contains(lowerQuery)) return true

        val lore = CC.strip(item.itemMeta?.lore?.joinToString(" ") ?: "").lowercase()
        if (lore.contains(lowerQuery)) return true

        if (item.type.name.lowercase().replace("_", " ").contains(lowerQuery)) return true

        return false
    }

    private fun getPreviousButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=")
            .name("&aPrevious Page").lore("", "&7(( Click me to go to the &f&oprevious &7page ))")
        return GuiItem(item) {
            if (gui.previous()) {
                viewer.playSound(viewer.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }

    private fun getNextButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=")
            .name("&aNext Page").lore("", "&7(( Click me to go to the &f&onext &7page ))")
        return GuiItem(item) {
            if (gui.next()) {
                viewer.playSound(viewer.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }
}
