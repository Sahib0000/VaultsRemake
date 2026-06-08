package net.carnagepvp.vaultsremake.core.util.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.net.URI
import java.net.URISyntaxException
import java.util.*

object SkullUtils {

    @JvmStatic
    fun createSkull(texture: String): ItemStack {
        val skullItem = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val meta = skullItem.itemMeta as SkullMeta
        val profile = GameProfile(UUID.randomUUID(), null)
        profile.properties.put("textures", Property("textures", texture))
        try {
            val profileField = meta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField[meta] = profile
        } catch (e: Exception) {
            e.printStackTrace()
        }
        skullItem.itemMeta = meta
        return skullItem
    }

    @JvmStatic
    fun fromName(type: Type, name: String): ItemStack {
        val item = ItemStack(type.mat, 1, 3.toShort())
        return withName(item, name)
    }

    @JvmStatic
    fun convertTexture(textureId: String): String {
        val url = "https://textures.minecraft.net/texture/$textureId"
        return Base64.getEncoder().encodeToString(
            "{\"textures\":{\"SKIN\":{\"url\":\"$url\"}}}".toByteArray()
        )
    }

    @JvmStatic
    fun withName(item: ItemStack, name: String): ItemStack {
        notNull(item, "item")
        notNull(name, "name")
        return Bukkit.getUnsafe().modifyItemStack(item, "{SkullOwner:\"$name\"}")
    }

    @JvmStatic
    fun fromUrl(type: Type, url: String): ItemStack {
        val item = ItemStack(type.mat, 1, 3.toShort())
        return withUrl(item, url)
    }

    @JvmStatic
    fun withUrl(item: ItemStack, url: String): ItemStack {
        notNull(url, "url")

        val actualUrl: URI
        try {
            actualUrl = URI(url)
        } catch (var4: URISyntaxException) {
            throw RuntimeException(var4)
        }

        var base64 = "{\"textures\":{\"SKIN\":{\"url\":\"$actualUrl\"}}}"
        base64 = Base64.getEncoder().encodeToString(base64.toByteArray())
        return withBase64(item, base64)
    }

    @JvmStatic
    fun fromBase64(type: Type, base64: String): ItemStack {
        val item = ItemStack(type.mat, 1, 3.toShort())
        return withBase64(item, base64)
    }

    @JvmStatic
    fun withBase64(item: ItemStack, base64: String): ItemStack {
        notNull(item, "item")
        notNull(base64, "base64")
        val hashAsId = UUID(base64.hashCode().toLong(), base64.hashCode().toLong())
        return Bukkit.getUnsafe().modifyItemStack(
            item,
            "{SkullOwner:{Id:\"$hashAsId\",Properties:{textures:[{Value:\"$base64\"}]}}}"
        )
    }

    private fun notNull(o: Any?, name: String) {
        if (o == null) {
            throw NullPointerException("$name should not be null!")
        }
    }

    @JvmStatic
    fun createSkullPlayer(playerName: String): ItemStack {
        val skullItem = ItemStack(Material.SKULL_ITEM, 1, 3.toShort())
        val meta = skullItem.itemMeta as SkullMeta
        meta.owner = playerName
        skullItem.itemMeta = meta
        return skullItem
    }

    enum class Type(val mat: Material) {
        BLOCK(Material.SKULL),
        ITEM(Material.SKULL_ITEM)
    }
}
