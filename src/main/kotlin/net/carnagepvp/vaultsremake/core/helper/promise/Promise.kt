package net.carnagepvp.vaultsremake.core.helper.promise

import net.carnagepvp.vaultsremake.core.helper.Delegates
import net.carnagepvp.vaultsremake.core.helper.terminable.Terminable
import org.bukkit.Bukkit
import net.carnagepvp.vaultsremake.VaultsPlugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier

class Promise<V>(val future: CompletableFuture<V>) : Terminable {

    companion object {
        fun <U> completed(value: U): Promise<U> {
            return Promise(CompletableFuture.completedFuture(value))
        }

        fun empty(): Promise<Void?> {
            return Promise(CompletableFuture.completedFuture(null))
        }

        fun <U> supplyingAsync(supplier: Supplier<U>): Promise<U> {
            val future = CompletableFuture.supplyAsync(supplier)
            return Promise(future)
        }

        fun <U> supplyingSync(supplier: Supplier<U>): Promise<U> {
            val future = CompletableFuture<U>()
            Bukkit.getScheduler().runTask(VaultsPlugin.instance) {
                try {
                    future.complete(supplier.get())
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            }
            return Promise(future)
        }

        fun <U> exceptionally(exception: Throwable): Promise<U> {
            val future = CompletableFuture<U>()
            future.completeExceptionally(exception)
            return Promise(future)
        }

        fun <U> fromFuture(future: CompletableFuture<U>): Promise<U> {
            return Promise(future)
        }
    }

    fun join(): V = future.join()
    
    fun <U> thenApplySync(fn: Function<in V, out U>): Promise<U> {
        val next = CompletableFuture<U>()
        future.whenComplete { v, t ->
            if (t != null) {
                next.completeExceptionally(t)
            } else {
                Bukkit.getScheduler().runTask(VaultsPlugin.instance) {
                    try {
                        next.complete(fn.apply(v))
                    } catch (e: Exception) {
                        next.completeExceptionally(e)
                    }
                }
            }
        }
        return Promise(next)
    }

    fun <U> thenApplyAsync(fn: Function<in V, out U>): Promise<U> {
        return Promise(future.thenApplyAsync(fn))
    }

    fun thenAcceptSync(action: Consumer<in V>): Promise<Void?> {
        val fn = Delegates.consumerToFunction(action)
        return thenApplySync(fn)
    }

    fun thenAcceptAsync(action: Consumer<in V>): Promise<Void?> {
        val fn = Delegates.consumerToFunction(action)
        return thenApplyAsync(fn)
    }

    fun <U> thenComposeSync(fn: Function<in V, out Promise<U>>): Promise<U> {
        val next = CompletableFuture<U>()
        future.whenComplete { v, t ->
            if (t != null) {
                next.completeExceptionally(t)
            } else {
                Bukkit.getScheduler().runTask(VaultsPlugin.instance) {
                    try {
                        val p = fn.apply(v)
                        p.future.whenComplete { v2, t2 ->
                            if (t2 != null) next.completeExceptionally(t2)
                            else next.complete(v2)
                        }
                    } catch (e: Exception) {
                        next.completeExceptionally(e)
                    }
                }
            }
        }
        return Promise(next)
    }

    fun exceptionally(fn: Function<Throwable, out V>): Promise<V> {
        val next = CompletableFuture<V>()
        future.whenComplete { v, t ->
            if (t == null) {
                next.complete(v)
            } else {
                try {
                    next.complete(fn.apply(t))
                } catch (e: Exception) {
                    next.completeExceptionally(e)
                }
            }
        }
        return Promise(next)
    }

    override fun close() {
        if (!future.isDone) {
            future.cancel(true)
        }
    }
}
