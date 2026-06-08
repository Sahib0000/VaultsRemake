package net.carnagepvp.vaultsremake.core.config

import net.carnagepvp.vaultsremake.core.config.annotations.ConfigField
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.charset.StandardCharsets

object ConfigManager {

    fun registerConfig(config: Configuration, file: File, plugin: JavaPlugin) {
        if (file.exists()) {
            loadConfig(config, file)
        }
        saveConfig(config, file)
    }

    fun loadConfig(config: Configuration, file: File) {
        val yaml = YamlConfiguration.loadConfiguration(file)
        val clazz = config.javaClass
        
        clazz.declaredFields.forEach { field ->
            val annotation = field.getAnnotation(ConfigField::class.java) ?: return@forEach
            val path = annotation.path.ifEmpty { field.name }
            
            field.isAccessible = true
            
            if (yaml.contains(path)) {
                val value = yaml.get(path)
                if (value != null) {
                    field.set(config, value)
                }
            }
        }
    }

    fun saveConfig(config: Configuration, file: File) {
        val yaml = YamlConfiguration()
        val clazz = config.javaClass
        val comments = mutableMapOf<String, Array<String>>()

        clazz.declaredFields.forEach { field ->
            val annotation = field.getAnnotation(ConfigField::class.java) ?: return@forEach
            val path = annotation.path.ifEmpty { field.name }
            
            field.isAccessible = true
            val value = field.get(config)
            yaml.set(path, value)
            if (annotation.comment.isNotEmpty()) {
                comments[path] = annotation.comment
            }
        }

        val lines = yaml.saveToString().split("\n").toMutableList()
        val result = mutableListOf<String>()
        val pathStack = mutableListOf<Pair<Int, String>>()

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("-")) {
                result.add(line)
                return@forEach
            }

            if (trimmed.contains(":")) {
                val indent = line.takeWhile { it == ' ' }.length
                val key = trimmed.substringBefore(":")

                while (pathStack.isNotEmpty() && pathStack.last().first >= indent) {
                    pathStack.removeAt(pathStack.size - 1)
                }
                pathStack.add(indent to key)

                val fullKey = pathStack.joinToString(".") { it.second }

                if (comments.containsKey(fullKey)) {
                    comments[fullKey]?.forEach { comment ->
                        val prefix = " ".repeat(indent)
                        result.add("$prefix# $comment")
                    }
                }
            }
            result.add(line)
        }

        file.writeText(result.joinToString("\n"), StandardCharsets.UTF_8)
    }
}
