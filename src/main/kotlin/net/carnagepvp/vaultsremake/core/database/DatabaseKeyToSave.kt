package net.carnagepvp.vaultsremake.core.database

abstract class DatabaseKeyToSave<K : Any> {

    @Transient
    private lateinit var key: K

    fun getKey(): K = this.key

    open fun setKey(key: K) {
        this.key = key
    }
}
