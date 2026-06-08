package net.carnagepvp.vaultsremake.core.mongo.plugin

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mongodb.MongoClientURI
import com.mongodb.MongoCredential
import com.mongodb.ServerAddress
import com.mongodb.client.MongoDatabase
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.core.mongo.Mongo
import net.carnagepvp.vaultsremake.core.mongo.MongoDatabaseCredentials
import net.carnagepvp.vaultsremake.core.util.chat.CC
import org.bukkit.Bukkit
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia
import org.mongodb.morphia.mapping.DefaultCreator

class HelperMongo(credentials: MongoDatabaseCredentials) : Mongo {

    override val client: MongoClient
    override val database: MongoDatabase
    override val morphia: Morphia
    override val morphiaDatastore: Datastore

    init {
        if (credentials.useUri) {
            this.client = MongoClient(MongoClientURI(credentials.uri))
        } else {
            val mongoCredential = MongoCredential.createCredential(
                credentials.username,
                credentials.authDatabase,
                credentials.password.toCharArray()
            )

            this.client = MongoClient(
                ServerAddress(credentials.address, credentials.port),
                listOf(mongoCredential),
                MongoClientOptions.builder().build()
            )
        }
        this.database = this.client.getDatabase(credentials.database)
        this.morphia = Morphia()
        this.morphiaDatastore = this.morphia.createDatastore(this.client, credentials.database)
        this.morphia.mapper.options.objectFactory = object : DefaultCreator() {
            override fun getClassLoaderForClass(): ClassLoader {
                return VaultsPlugin.instance.javaClass.classLoader
            }
        }

        try {
            this.client.address // Force a check
            val connectionString = if (credentials.useUri) "URI" else "${credentials.address}:${credentials.port}"
            Bukkit.getConsoleSender().sendMessage(CC.translate("&a[Mongo] Successfully connected to $connectionString (${credentials.database})"))
        } catch (e: Exception) {
            val connectionString = if (credentials.useUri) "URI" else "${credentials.address}:${credentials.port}"
            Bukkit.getConsoleSender().sendMessage(CC.translate("&c[Mongo] Failed to connect to $connectionString (${credentials.database})"))
        }
    }

    override fun isConnected(): Boolean {
        return try {
            client.address != null
        } catch (e: Exception) {
            false
        }
    }

    override fun getDatabase(name: String): MongoDatabase {
        return client.getDatabase(name)
    }

    override fun close() {
        client.close()
    }

    override fun getMorphiaDatastore(name: String): Datastore {
        return morphia.createDatastore(client, name)
    }
}
