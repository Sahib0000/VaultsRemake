package net.carnagepvp.vaultsremake.core.helper

import java.util.function.Consumer
import java.util.function.Function

/**
 * Utility class for converting between functional interfaces.
 */
object Delegates {
    @JvmStatic
    fun <T> consumerToFunction(consumer: Consumer<in T>): Function<T, Void?> {
        return Function { t: T ->
            consumer.accept(t)
            null
        }
    }

    @JvmStatic
    fun <T> runnableToFunction(runnable: Runnable): Function<T, Void?> {
        return Function { _: T ->
            runnable.run()
            null
        }
    }
}
