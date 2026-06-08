package net.carnagepvp.vaultsremake.core.util.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.carnagepvp.vaultsremake.core.util.chat.CC
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.material.MaterialData
import java.util.*

open class ItemBuilder : ItemStack {

    constructor(material: Material) : super(material)
    constructor(material: Material, damage: Byte) : super(material, 1, damage.toShort())
    constructor(itemStack: ItemStack) : super(itemStack)

    fun amount(amount: Int): ItemBuilder {
        this.amount = amount
        return this
    }

    fun name(name: String, vararg arguments: Any?): ItemBuilder {
        val meta = itemMeta ?: return this
        meta.displayName = CC.format(name, *arguments)
        itemMeta = meta
        return this
    }

    fun appendName(append: String, vararg arguments: Any?): ItemBuilder {
        val meta = itemMeta ?: return this
        val currentName = if (meta.hasDisplayName()) meta.displayName else ""
        meta.displayName = CC.translate(currentName + CC.format(append, *arguments))
        itemMeta = meta
        return this
    }

    fun lore(text: String): ItemBuilder {
        val meta = itemMeta ?: return this
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        lore.add(CC.translate(text))
        meta.lore = lore
        itemMeta = meta
        return this
    }

    fun lore(vararg text: String): ItemBuilder {
        val meta = itemMeta ?: return this
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        text.forEach { lore.add(CC.translate(it)) }
        meta.lore = lore
        itemMeta = meta
        return this
    }

    fun lore(text: List<String>): ItemBuilder {
        val meta = itemMeta ?: return this
        val lore = meta.lore?.toMutableList() ?: mutableListOf()
        text.forEach { lore.add(CC.translate(it)) }
        meta.lore = lore
        itemMeta = meta
        return this
    }

    fun durability(durability: Int): ItemBuilder {
        this.durability = durability.toShort()
        return this
    }

    @Suppress("DEPRECATION")
    fun data(data: Int): ItemBuilder {
        setData(MaterialData(type, data.toByte()))
        return this
    }

    fun enchantment(enchantment: Enchantment, level: Int = 1): ItemBuilder {
        addUnsafeEnchantment(enchantment, level)
        return this
    }

    fun enchantments(enchantments: Map<Enchantment, Int>): ItemBuilder {
        addUnsafeEnchantments(enchantments)
        return this
    }

    fun type(material: Material): ItemBuilder {
        type = material
        return this
    }

    fun clearLore(): ItemBuilder {
        val meta = itemMeta ?: return this
        meta.lore = mutableListOf()
        itemMeta = meta
        return this
    }

    fun clearEnchantments(): ItemBuilder {
        enchantments.keys.forEach { removeEnchantment(it) }
        return this
    }

    fun owner(owner: String): ItemBuilder {
        val meta = itemMeta
        if (meta !is SkullMeta) return this

        meta.owner = owner
        itemMeta = meta
        return this
    }

    fun texture(value: String): ItemBuilder {
        val meta = itemMeta
        if (meta !is SkullMeta) return this

        try {
            val profile = GameProfile(UUID.randomUUID(), null)
            profile.properties.put("textures", Property("textures", value))

            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(meta, profile)

            itemMeta = meta
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return this
    }

    fun color(color: Color): ItemBuilder {
        val meta = itemMeta as? LeatherArmorMeta ?: return this
        meta.color = color
        itemMeta = meta
        return this
    }

    fun glow(glow: Boolean = true): ItemBuilder {
        if (glow) {
            addUnsafeEnchantment(Enchantment.DURABILITY, 0)
            flag(ItemFlag.HIDE_ENCHANTS)
        }
        return this
    }

    fun flag(flag: ItemFlag): ItemBuilder {
        val meta = itemMeta ?: return this
        meta.addItemFlags(flag)
        itemMeta = meta
        return this
    }

    fun flags(vararg flags: ItemFlag): ItemBuilder {
        val meta = itemMeta ?: return this
        meta.addItemFlags(*flags)
        itemMeta = meta
        return this
    }

    fun clearFlags(): ItemBuilder {
        val meta = itemMeta ?: return this
        meta.itemFlags.forEach { meta.removeItemFlags(it) }
        itemMeta = meta
        return this
    }

    companion object {
        private val LEATHER_ARMOR_TYPES = setOf(
            Material.LEATHER_BOOTS,
            Material.LEATHER_CHESTPLATE,
            Material.LEATHER_HELMET,
            Material.LEATHER_LEGGINGS
        )
    }
}
