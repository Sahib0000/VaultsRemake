package net.carnagepvp.vaultsremake.core.config

import org.bukkit.plugin.java.JavaPlugin
import java.io.File

abstract class ConfigurationBase(val fileName: String) : Configuration {

    fun init(plugin: JavaPlugin) {
        if (!plugin.dataFolder.exists()) {
            plugin.dataFolder.mkdirs()
        }
        val file = File(plugin.dataFolder, fileName)
        if (!file.exists()) {
            plugin.saveResource(fileName, false)
        }
        ConfigManager.registerConfig(this, file, plugin)
    }

    fun save(plugin: JavaPlugin) {
        val file = File(plugin.dataFolder, fileName)
        ConfigManager.saveConfig(this, file)
    }
}
