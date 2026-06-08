package net.carnagepvp.vaultsremake.gui

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import dev.triumphteam.gui.guis.PaginatedGui
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemBuilder
import net.carnagepvp.vaultsremake.core.util.item.ItemSerializer
import net.carnagepvp.vaultsremake.data.VaultData
import net.carnagepvp.vaultsremake.struct.VaultManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.ItemStack

class VaultSelectorGui(
    private val player: Player,
    private val vaultManager: VaultManager,
    private val allVaults: List<VaultData>
) {

    private val gui: PaginatedGui = Gui.paginated()
        .title(Component.text(CC.translate(VaultsConfig.selectorTitle)))
        .rows(4)
        .pageSize(27)
        .disableAllInteractions()
        .create()

    fun open() {
        val maxVaults = vaultManager.getMaxVaults(player)
        
        gui.clearPageItems()

        allVaults.forEachIndexed { index, data ->
            val vaultNumber = index + 1
            if (vaultNumber > maxVaults) return@forEachIndexed

            val icon = if (data.iconBase64 != null) {
                ItemSerializer.fromBase64(data.iconBase64!!).firstOrNull() ?: ItemStack(Material.CHEST)
            } else {
                ItemStack(Material.CHEST)
            }

            val displayName = VaultsConfig.vaultItemName.replace("{0}", vaultNumber.toString())

            val item = ItemBuilder(icon)
                .name(displayName)
                .clearLore()
                .lore(
                    "",
                    "&b&lInformation",
                    "&b┆ &7Vault ID: &f#$vaultNumber",
                    "&b┆ &7Status: &fUnlocked",
                    "",
                    "&b&lUsage",
                    "&b┆ &7Left-Click: &fOpen Vault",
                    "&b┆ &7Right-Click: &fSelect Icon",
                    "",
                    "&7(( Click me to open this vault ))"
                )

            val guiItem = GuiItem(item) { event ->
                when (event.click) {
                    ClickType.LEFT -> {
                        VaultGui(player, vaultNumber, data, vaultManager).open()
                    }
                    ClickType.RIGHT -> {
                        VaultMaterialSelectorGui(player, vaultNumber, data, vaultManager).open()
                    }
                    else -> {}
                }
            }

            gui.addItem(guiItem)
        }

        gui.filler.fillBottom(GuiItem(ItemBuilder(Material.STAINED_GLASS_PANE).durability(7).name("&7")))
        gui.setItem(30, getPreviousButton())
        gui.setItem(32, getNextButton())

        gui.open(player)
    }

    private fun getPreviousButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWYxMzNlOTE5MTlkYjBhY2VmZGMyNzJkNjdmZDg3YjRiZTg4ZGM0NGE5NTg5NTg4MjQ0NzRlMjFlMDZkNTNlNiJ9fX0=")
            .name("&aPrevious Page").lore("", "&7(( Click me to go to the &f&oprevious &7page ))")
        return GuiItem(item) {
            if (gui.previous()) {
                player.playSound(player.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }

    private fun getNextButton(): GuiItem {
        val item = ItemBuilder(Material.SKULL_ITEM).durability(3)
            .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTNmYzUyMjY0ZDhhZDllNjU0ZjQxNWJlZjAxYTIzOTQ3ZWRiY2NjY2Y2NDkzNzMyODliZWE0ZDE0OTU0MWY3MCJ9fX0=")
            .name("&aNext Page").lore("", "&7(( Click me to go to the &f&onext &7page ))")
        return GuiItem(item) {
            if (gui.next()) {
                player.playSound(player.location, Sound.BAT_TAKEOFF, 0.75f, 0.75f)
            }
        }
    }
}
