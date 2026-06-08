package net.carnagepvp.vaultsremake.core.util.serialize

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Type

class ItemStackSerializer : JsonSerializer<ItemStack> {

    @Throws(IOException::class)
    private fun serialize(itemStack: ItemStack): String {
        val bytesOut = ByteArrayOutputStream()
        val out = BukkitObjectOutputStream(bytesOut)
        out.writeObject(itemStack)
        out.flush()
        out.close()
        return Base64Coder.encodeLines(bytesOut.toByteArray())
    }

    override fun serialize(itemStack: ItemStack, type: Type, context: JsonSerializationContext): JsonElement {
        val serialized = try {
            serialize(itemStack)
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
        val jsonObject = JsonObject()
        jsonObject.addProperty("item", serialized)
        return jsonObject.get("item")
    }
}
