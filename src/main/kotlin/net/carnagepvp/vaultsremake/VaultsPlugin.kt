package net.carnagepvp.vaultsremake

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import co.aikar.commands.BukkitCommandManager
import net.carnagepvp.vaultsremake.core.mongo.MongoDatabaseCredentials
import net.carnagepvp.vaultsremake.core.mongo.plugin.HelperMongo
import net.carnagepvp.vaultsremake.core.util.chat.CC
import net.carnagepvp.vaultsremake.core.util.item.ItemBuilder
import net.carnagepvp.vaultsremake.core.util.serialize.ItemStackDeserializer
import net.carnagepvp.vaultsremake.core.util.serialize.ItemStackSerializer
import net.carnagepvp.vaultsremake.command.VaultCommand
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.listener.VaultListener
import net.carnagepvp.vaultsremake.struct.VaultManager
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.serialization.ConfigurationSerialization
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin

class VaultsPlugin : JavaPlugin() {

    companion object {
        @JvmStatic
        lateinit var instance: VaultsPlugin

        fun prefix(message: String, vararg arguments: Any): String {
            return CC.translate(CC.format("${VaultsConfig.prefix} &8&l➸&7 $message", *arguments))
        }
    }

    lateinit var commandManager: BukkitCommandManager
        private set

    lateinit var mongo: HelperMongo
        private set

    lateinit var gson: com.google.gson.Gson
        private set

    lateinit var vaultManager: VaultManager
        private set

    override fun onEnable() {
        instance = this
        commandManager = BukkitCommandManager(this)
        commandManager.enableUnstableAPI("help")

        ConfigurationSerialization.registerClass(ItemBuilder::class.java)

        VaultsConfig.init(this)

        val credentials = MongoDatabaseCredentials(
            VaultsConfig.mongoAddress,
            VaultsConfig.mongoPort,
            VaultsConfig.mongoDatabase,
            VaultsConfig.mongoUsername,
            VaultsConfig.mongoPassword,
            VaultsConfig.mongoAuthDatabase
        )
        try {
            mongo = HelperMongo(credentials)
        } catch (e: Exception) {
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c[VaultsRemake] Failed to initialize Mongo!"))
        }

        gson = GsonBuilder()
            .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackSerializer()).setLenient()
            .registerTypeHierarchyAdapter(ItemStack::class.java, ItemStackDeserializer()).setLenient()
            .setFieldNamingPolicy(FieldNamingPolicy.IDENTITY).create()

        vaultManager = VaultManager(mongo, gson)

        commandManager.registerCommand(VaultCommand(vaultManager))
        server.pluginManager.registerEvents(VaultListener(vaultManager), this)
    }

    override fun onDisable() {
        if (::vaultManager.isInitialized) {
            vaultManager.shutdown()
        }
        if (::commandManager.isInitialized) {
            commandManager.unregisterCommands()
        }
        if (::mongo.isInitialized) {
            mongo.close()
        }
    }
}
