package net.carnagepvp.vaultsremake.core.database

import com.google.gson.Gson
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.ReplaceOptions
import net.carnagepvp.vaultsremake.core.helper.promise.Promise
import net.carnagepvp.vaultsremake.core.mongo.plugin.HelperMongo
import org.bson.Document
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

abstract class MongoSaveLoadManager<K : Any, T : DatabaseKeyToSave<K>>(
    protected val mongo: HelperMongo,
    private val gson: Gson,
    protected val clazz: Class<T>
) {

    abstract val collectionName: String

    @get:JvmName("getCacheMap")
    protected val cache: ConcurrentHashMap<K, T> = ConcurrentHashMap()
    private val isShuttingDown = AtomicBoolean(false)

    protected val collection: MongoCollection<Document> by lazy {
        mongo.database.getCollection(collectionName)
    }

    fun hasCache(): Boolean = cache.isNotEmpty()

    open fun loadAll(): Promise<Void?> {
        return Promise.supplyingAsync {
            val cursor = collection.find().iterator()
            cursor.use { cursor ->
                while (cursor.hasNext()) {
                    val doc = cursor.next()
                    val idStr = doc.getString("_id") ?: continue
                    val key = parseKey(idStr)

                    val dataJson = doc.toJson()
                    val t = deserialize(dataJson)

                    if (t != null) {
                        t.setKey(key)
                        cache[key] = t
                    }
                }
            }
            null
        }
    }

    open fun load(key: K): Promise<T?> {
        val cached = getFromCache(key)
        if (cached != null) return Promise.completed(cached)

        return Promise.supplyingAsync {
            val doc = collection.find(Document("_id", serializeKey(key))).first()
            if (doc != null) {
                val t = deserialize(doc.toJson())
                t?.also {
                    it.setKey(key)
                    cache[key] = it
                }
            } else null
        }
    }

    fun save(t: T): Promise<Void?> {
        if (isShuttingDown.get()) {
            return Promise.exceptionally(IllegalStateException("Manager is shutting down"))
        }

        val key = t.getKey()
        cache[key] = t

        return Promise.supplyingAsync {
            val serializedJson = serialize(t)

            val doc = Document.parse(serializedJson).append("_id", serializeKey(key))

            collection.replaceOne(
                Document("_id", serializeKey(key)),
                doc,
                ReplaceOptions().upsert(true)
            )
            null
        }
    }

    protected abstract fun parseKey(id: String): K

    protected open fun serializeKey(key: K): String = key.toString()

    fun addToCache(t: T) { cache[t.getKey()] = t }

    fun removeFromCache(key: K) { cache.remove(key) }

    fun getFromCache(key: K): T? = cache[key]

    fun getOrCreate(key: K, factory: (K) -> T): T {
        var data = getFromCache(key)
        if (data == null) {
            data = factory(key)
            data.setKey(key)
            save(data)
        }
        return data
    }

    fun contains(key: K): Boolean = cache.containsKey(key)

    fun clearCache() { cache.clear() }

    fun purge(): Promise<Void?> {
        if (isShuttingDown.get()) {
            return Promise.exceptionally(IllegalStateException("Manager is shutting down"))
        }
        clearCache()
        return Promise.supplyingAsync {
            collection.drop()
            null
        }
    }

    open fun delete(key: K): Promise<Long> {
        if (isShuttingDown.get()) {
            return Promise.exceptionally(IllegalStateException("Manager is shutting down"))
        }
        removeFromCache(key)

        return Promise.supplyingAsync {
            val result = collection.deleteOne(Document("_id", serializeKey(key)))
            result.deletedCount
        }
    }

    fun getCache(): Map<K, T> = cache.toMap()

    fun getCacheValues(): Collection<T> = cache.values

    fun saveAll() {
        cache.values.forEach { save(it) }
    }

    fun shutdown() {
        isShuttingDown.set(true)
        cache.clear()
    }

    open fun serialize(t: T): String = gson.toJson(t)

    open fun deserialize(json: String): T? = try {
        gson.fromJson(json, clazz)
    } catch (e: Exception) {
        null
    }
}
