package net.carnagepvp.vaultsremake.gui

import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemBuilder
import net.carnagepvp.vaultsremake.core.util.item.ItemSerializer
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.data.VaultData
import net.carnagepvp.vaultsremake.struct.VaultManager
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class VaultMaterialSelectorGui(
    private val player: Player,
    private val vaultNumber: Int,
    private val vaultData: VaultData,
    private val vaultManager: VaultManager
) {

    private val allowedMaterials = listOf(
        Material.DIAMOND_SWORD, Material.DIAMOND_AXE, Material.DIAMOND_BOOTS, Material.DIAMOND_LEGGINGS, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_HELMET,
        Material.GOLD_SWORD, Material.GOLD_AXE, Material.GOLD_BOOTS, Material.GOLD_LEGGINGS, Material.GOLD_CHESTPLATE, Material.GOLD_HELMET,
        Material.DIAMOND, Material.EMERALD, Material.GOLD_INGOT, Material.IRON_INGOT, Material.REDSTONE, Material.GOLDEN_APPLE,
        Material.COOKED_BEEF, Material.SKULL_ITEM, Material.FISHING_ROD, Material.CHEST
    )

    fun open() {
        val gui = Gui.gui()
            .title(Component.text(CC.translate(VaultsConfig.materialSelectorTitle)))
            .rows(3)
            .disableAllInteractions()
            .create()

        allowedMaterials.forEach { material ->
            val itemStack = when (material) {
                Material.SKULL_ITEM -> ItemStack(material, 1, 1)
                Material.GOLDEN_APPLE -> ItemStack(material, 1, 1)
                else -> ItemStack(material)
            }

            val item = ItemBuilder(itemStack)
                .name("&b&l${material.name.replace("_", " ")}")
                .clearLore()
                .lore("&7Click to select this icon")

            val guiItem = GuiItem(item) {
                vaultData.iconBase64 = ItemSerializer.toBase64(arrayOf(itemStack))
                vaultManager.save(vaultData)
                player.sendMessage(VaultsPlugin.prefix(VaultsConfig.vaultIconUpdated, vaultNumber))

                vaultManager.getAllVaults(player).thenAcceptSync { allVaults ->
                    VaultSelectorGui(player, vaultManager, allVaults).open()
                }
            }
            gui.addItem(guiItem)
        }

        gui.open(player)
    }
}
