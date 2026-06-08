package net.carnagepvp.vaultsremake.struct

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import net.carnagepvp.vaultsremake.core.helper.promise.Promise
import net.carnagepvp.vaultsremake.core.mongo.plugin.HelperMongo
import net.carnagepvp.vaultsremake.core.database.MongoSaveLoadManager
import net.carnagepvp.vaultsremake.VaultsPlugin
import net.carnagepvp.vaultsremake.config.VaultsConfig
import net.carnagepvp.vaultsremake.data.VaultData
import org.bson.Document
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*
import java.util.concurrent.CompletableFuture

class VaultManager(
    mongo: HelperMongo,
    gson: Gson
) : MongoSaveLoadManager<String, VaultData>(mongo, gson, VaultData::class.java) {

    private val viewTokens = mutableMapOf<String, Long>()

    override val collectionName: String = "vaults"
    
    private val backupCollection: MongoCollection<Document> by lazy {
        mongo.database.getCollection("vault_backup")
    }

    fun isConnected(): Boolean = mongo.isConnected()

    override fun parseKey(id: String): String = id

    fun getOrCreateData(uuid: UUID, vaultNumber: Int): VaultData {
        val compositeKey = "${uuid}:${vaultNumber}"
        return getOrCreate(compositeKey) { key ->
            VaultData()
        }
    }

    fun getAllVaults(player: Player): Promise<List<VaultData>> {
        val maxVaults = getMaxVaults(player)
        return getAllVaults(player.uniqueId, maxVaults)
    }

    fun getAllVaults(uuid: UUID, maxVaults: Int): Promise<List<VaultData>> {
        val futures = (1..maxVaults).map { vaultNumber ->
            load("${uuid}:${vaultNumber}").future.thenApply { data ->
                data ?: VaultData().apply { setKey("${uuid}:${vaultNumber}") }
            }.exceptionally { ex ->
                VaultsPlugin.instance.logger.severe("Failed to load vault $vaultNumber for $uuid: ${ex.message}")
                VaultData().apply { setKey("${uuid}:${vaultNumber}") }
            }
        }
        
        val combinedFuture = CompletableFuture.allOf(*futures.toTypedArray())
            .thenApply { _ -> futures.map { it.join() } }
            
        return Promise.fromFuture(combinedFuture)
    }

    fun getMaxVaults(player: Player): Int {
        if (player.hasPermission("vaultsremake.amount.*") || player.isOp) {
            return 50
        }

        return player.effectivePermissions
            .filter { it.permission.startsWith("vaultsremake.amount.") && it.value }
            .mapNotNull { it.permission.removePrefix("vaultsremake.amount.").toIntOrNull() }
            .maxOrNull() ?: VaultsConfig.defaultMaxVaults
    }

    fun isBlacklisted(item: ItemStack?): Boolean {
        if (item == null || item.type == org.bukkit.Material.AIR) return false
        return VaultsConfig.blacklistedItems.contains(item.type.name)
    }

    fun deleteVault(uuid: UUID, vaultNumber: Int): Promise<Long> {
        val compositeKey = "${uuid}:${vaultNumber}"
        return backupVault(uuid, vaultNumber).thenComposeSync {
            delete(compositeKey)
        }
    }

    fun backupVault(uuid: UUID, vaultNumber: Int): Promise<Void?> {
        val compositeKey = "${uuid}:${vaultNumber}"
        return load(compositeKey).thenComposeSync { data ->
            if (data == null) return@thenComposeSync Promise.empty()

            Promise.supplyingAsync {
                val serializedJson = serialize(data)
                val doc = Document.parse(serializedJson)
                    .append("_id", "${compositeKey}:${System.currentTimeMillis()}")
                    .append("original_id", compositeKey)
                    .append("backup_at", System.currentTimeMillis())
                
                backupCollection.insertOne(doc)
                null
            }
        }
    }

    fun purgeDatabase(): Promise<Void?> {
        clearCache()
        return Promise.supplyingAsync {
            collection.drop()
            null
        }
    }

    fun handleLogout(player: Player) {
        val uuidPrefix = "${player.uniqueId}:"
        val keysToRemove = cache.keys().asSequence().filter { it.startsWith(uuidPrefix) }.toList()

        keysToRemove.forEach { key ->
            val data = cache[key] ?: return@forEach
            save(data)
            removeFromCache(key)
        }
    }

    fun deleteAllVaults(uuid: UUID): Promise<Void?> {
        val uuidPrefix = "${uuid}:"
        
        val keysToRemove = cache.keys().asSequence().filter { it.startsWith(uuidPrefix) }.toList()
        keysToRemove.forEach { removeFromCache(it) }

        return backupAllVaults(uuid).thenComposeSync {
            Promise.supplyingAsync {
                val filter = Document("_id", Document("\$regex", "^$uuidPrefix"))
                collection.deleteMany(filter)
                null
            }
        }
    }

    fun backupAllVaults(uuid: UUID): Promise<Void?> {
        val uuidPrefix = "${uuid}:"
        return Promise.supplyingAsync {
            val filter = Document("_id", Document("\$regex", "^$uuidPrefix"))
            val documents = collection.find(filter).toList()
            
            if (documents.isNotEmpty()) {
                val timestamp = System.currentTimeMillis()
                val backupDocs = documents.map { doc ->
                    val originalId = doc.getString("_id")
                    Document(doc)
                        .append("_id", "$originalId:$timestamp")
                        .append("original_id", originalId)
                        .append("backup_at", timestamp)
                }
                backupCollection.insertMany(backupDocs)
            }
            null
        }
    }

    fun restoreVault(uuid: UUID, vaultNumber: Int): Promise<Boolean> {
        val compositeKey = "${uuid}:${vaultNumber}"
        return Promise.supplyingAsync {
            val filter = Document("original_id", compositeKey)
            val sort = Document("backup_at", -1)
            val backupDoc = backupCollection.find(filter).sort(sort).first() ?: return@supplyingAsync false

            restoreVaultFromDoc(uuid, vaultNumber, backupDoc)
            true
        }
    }

    fun restoreVaultFromDoc(uuid: UUID, vaultNumber: Int, backupDoc: Document) {
        val compositeKey = "${uuid}:${vaultNumber}"
        
        val doc = Document(backupDoc)
        doc.remove("_id")
        doc.remove("original_id")
        doc.remove("backup_at")

        val json = doc.toJson()
        val data = deserialize(json) ?: return
        
        data.setKey(compositeKey)
        save(data)
    }

    fun getBackups(uuid: UUID, vaultNumber: Int): Promise<List<Document>> {
        val compositeKey = "${uuid}:${vaultNumber}"
        return Promise.supplyingAsync {
            val filter = Document("original_id", compositeKey)
            val sort = Document("backup_at", -1)
            backupCollection.find(filter).sort(sort).toList()
        }
    }

    fun generateViewToken(ownerUuid: UUID, vaultNumber: Int): String {
        val token = UUID.randomUUID().toString().substring(0, 8)
        val key = "$ownerUuid:$vaultNumber:$token"
        viewTokens[key] = System.currentTimeMillis() + 60000 // 1 min
        return token
    }

    fun isValidViewToken(ownerUuid: UUID, vaultNumber: Int, token: String): Boolean {
        val key = "$ownerUuid:$vaultNumber:$token"
        val expiry = viewTokens[key] ?: return false
        if (System.currentTimeMillis() > expiry) {
            viewTokens.remove(key)
            return false
        }
        return true
    }
}
