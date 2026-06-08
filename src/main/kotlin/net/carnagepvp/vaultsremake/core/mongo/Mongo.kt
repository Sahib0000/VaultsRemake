package net.carnagepvp.vaultsremake.core.mongo

import com.mongodb.MongoClient
import com.mongodb.client.MongoDatabase
import net.carnagepvp.vaultsremake.core.helper.terminable.Terminable
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia

/**
 * Represents an individual Mongo datasource, created by the library.
 */
interface Mongo : Terminable {

    /**
     * Gets the client instance backing the datasource
     *
     * @return the client instance
     */
    val client: MongoClient

    /**
     * Gets the main database in use by the instance.
     *
     * @return the main database
     */
    val database: MongoDatabase

    /**
     * Gets a specific database instance
     *
     * @param name the name of the database
     * @return the database
     */
    fun getDatabase(name: String): MongoDatabase

    /**
     * Gets the Morphia instance for this datasource
     *
     * @return the morphia instance
     */
    val morphia: Morphia

    /**
     * Gets the main Morphia datastore in use by the instance
     *
     * @return the main datastore
     */
    val morphiaDatastore: Datastore

    /**
     * Gets a specific Morphia datastore instance
     *
     * @param name the name of the database
     * @return the datastore
     */
    fun getMorphiaDatastore(name: String): Datastore

    /**
     * Checks if the client is connected to the database.
     *
     * @return true if connected
     */
    fun isConnected(): Boolean

}
