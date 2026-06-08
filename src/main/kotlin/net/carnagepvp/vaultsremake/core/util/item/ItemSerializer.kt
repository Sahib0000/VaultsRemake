package net.carnagepvp.vaultsremake.core.util.item

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

object ItemSerializer {
    
    @JvmStatic
    fun toBase64(items: Array<ItemStack?>): String {
        try {
            ByteArrayOutputStream().use { outputStream ->
                BukkitObjectOutputStream(outputStream).use { dataOutput ->
                    dataOutput.writeInt(items.size)
                    for (item in items) {
                        dataOutput.writeObject(item)
                    }
                    return Base64Coder.encodeLines(outputStream.toByteArray())
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    @JvmStatic
    fun fromBase64(data: String): Array<ItemStack?> {
        if (data.isEmpty()) return arrayOfNulls(0)
        try {
            ByteArrayInputStream(Base64Coder.decodeLines(data)).use { inputStream ->
                BukkitObjectInputStream(inputStream).use { dataInput ->
                    val size = dataInput.readInt()
                    val items = arrayOfNulls<ItemStack>(size)
                    for (i in 0 until size) {
                        items[i] = dataInput.readObject() as? ItemStack
                    }
                    return items
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return arrayOfNulls(0)
        }
    }
}
