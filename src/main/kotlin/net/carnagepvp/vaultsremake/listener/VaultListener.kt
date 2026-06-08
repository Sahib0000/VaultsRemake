package net.carnagepvp.vaultsremake.listener

import net.carnagepvp.vaultsremake.struct.VaultManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class VaultListener(private val vaultManager: VaultManager) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        vaultManager.getAllVaults(event.player)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        vaultManager.handleLogout(event.player)
    }

}
