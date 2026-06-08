package net.carnagepvp.vaultsremake.core.helper.promise

/**
 * Represents the context in which a task should be executed.
 */
enum class ThreadContext {
    /**
     * Executes the task on the main server thread.
     */
    SYNC,

    /**
     * Executes the task asynchronously.
     */
    ASYNC
}
