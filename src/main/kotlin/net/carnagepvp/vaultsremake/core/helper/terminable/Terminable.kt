package net.carnagepvp.vaultsremake.core.helper.terminable

/**
 * An extension of [AutoCloseable] that does not throw any checked exceptions.
 */
fun interface Terminable : AutoCloseable {

    companion object {
        @JvmField
        val EMPTY = Terminable { }
    }

    /**
     * Closes the resource.
     */
    override fun close()

    /**
     * Closes the resource, and logs any exceptions.
     *
     * @return true if the resource was closed without exception, false otherwise
     */
    fun closeAndReportException(): Boolean {
        return try {
            close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Composite this terminable with another.
     *
     * @param other the other terminable
     * @return a new terminable
     */
    fun composite(other: Terminable): Terminable {
        return Terminable {
            other.use {
                this.close()
            }
        }
    }
}
