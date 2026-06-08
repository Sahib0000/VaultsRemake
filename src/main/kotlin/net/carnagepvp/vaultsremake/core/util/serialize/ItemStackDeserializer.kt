package net.carnagepvp.vaultsremake.core.util.serialize

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.IOException
import java.lang.reflect.Type

class ItemStackDeserializer : JsonDeserializer<ItemStack> {

    @Throws(IOException::class, ClassNotFoundException::class)
    fun deserialize(base64: String): ItemStack {
        val data = Base64Coder.decodeLines(base64)
        val bytesIn = ByteArrayInputStream(data)
        val `in` = BukkitObjectInputStream(bytesIn)
        val itemStack = `in`.readObject() as ItemStack
        `in`.close()
        return itemStack
    }

    @Throws(JsonParseException::class)
    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): ItemStack? {
        return try {
            deserialize(jsonElement.asJsonPrimitive.asString)
        } catch (e: Exception) {
            null
        }
    }
}
