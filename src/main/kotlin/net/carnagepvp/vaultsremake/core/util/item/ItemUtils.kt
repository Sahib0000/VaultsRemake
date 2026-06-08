package net.carnagepvp.vaultsremake.core.util.item

import org.apache.commons.lang.StringUtils
import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object ItemUtils {

    fun getInventorySize(size: Int): Int {
        for (i in 9..54 step 9) {
            if (size <= i) {
                return i
            }
        }
        return 54
    }

    fun moveItemWithoutStacking(item: ItemStack, targetInventory: Inventory): Int {
        var moved = 0
        var remaining = item.amount

        while (remaining > 0) {
            val emptySlot = targetInventory.firstEmpty()
            if (emptySlot == -1) break

            val single = item.clone()
            single.amount = 1
            targetInventory.setItem(emptySlot, single)

            remaining--
            moved++
        }

        return moved
    }

     fun isSimilar(item: ItemStack, other: ItemStack): Boolean {
        if (item.type != other.type) return false
        val itemHasMeta = item.hasItemMeta()
        val otherHasMeta = other.hasItemMeta()
        if (!itemHasMeta && !otherHasMeta) return true
        if (itemHasMeta != otherHasMeta) return false
        return item.itemMeta == other.itemMeta
    }

    fun getDisplayName(item: ItemStack): String =
        item.itemMeta?.takeIf { it.hasDisplayName() }?.displayName ?: item.type.name.lowercase().replace("_", " ")
            .split(" ").joinToString(" ") {
                it.replaceFirstChar(Char::uppercase)
            }

    fun getItemName(item: ItemStack): String {
        return when (item.type) {
            Material.GREEN_RECORD -> "Discs"
            Material.DRAGON_EGG -> "All Other Blocks"
            Material.MOB_SPAWNER -> "Monster Spawners"
            Material.POTION -> "Potions"
            Material.MONSTER_EGG -> "Spawner Eggs"
            Material.DIODE -> "Redstone Repeater"
            Material.REDSTONE_TORCH_ON -> "Redstone Torch"
            Material.INK_SACK -> if (item.durability.toInt() == 4) "Lapis Lazuli" else StringUtils.capitaliseAllWords(item.type.name.lowercase().replace("_", " "))
            Material.PISTON_BASE -> "Piston"
            Material.PISTON_STICKY_BASE -> "Sticky Piston"
            Material.REDSTONE_LAMP_OFF -> "Redstone Lamp"
            Material.SKULL_ITEM -> "Player Heads"
            else -> StringUtils.capitaliseAllWords(item.type.name.lowercase().replace("_", " "))
        }
    }


    fun cloneOrAir(item: ItemStack?): ItemStack {
        return if (item == null || item.type == Material.AIR) ItemStack(Material.AIR) else item.clone()
    }
}
