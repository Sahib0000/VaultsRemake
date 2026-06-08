package net.carnagepvp.vaultsremake.core.mongo

import org.bukkit.configuration.ConfigurationSection

/**
 * Represents the credentials for a remote database.
 */
data class MongoDatabaseCredentials(
    val address: String,
    val port: Int,
    val database: String,
    val username: String,
    val password: String,
    val authDatabase: String = database,
    val useUri: Boolean = false,
    val uri: String = ""
) {
    companion object {
        @JvmStatic
        fun of(address: String, port: Int, database: String, username: String, password: String): MongoDatabaseCredentials {
            return MongoDatabaseCredentials(address, port, database, username, password, database)
        }

        @JvmStatic
        fun of(address: String, port: Int, database: String, username: String, password: String, authDatabase: String): MongoDatabaseCredentials {
            return MongoDatabaseCredentials(address, port, database, username, password, authDatabase)
        }

        @JvmStatic
        fun of(uri: String, database: String): MongoDatabaseCredentials {
            return MongoDatabaseCredentials("", 0, database, "", "", database, true, uri)
        }

        @JvmStatic
        fun fromConfig(config: ConfigurationSection): MongoDatabaseCredentials {
            return MongoDatabaseCredentials(
                config.getString("address", "localhost")!!,
                config.getInt("port", 27017),
                config.getString("database", "minecraft")!!,
                config.getString("username", "root")!!,
                config.getString("password", "passw0rd")!!,
                config.getString("auth-database", config.getString("database", "minecraft")!!)!!,
                config.getBoolean("use-uri", false),
                config.getString("uri", "")!!
            )
        }
    }
}
