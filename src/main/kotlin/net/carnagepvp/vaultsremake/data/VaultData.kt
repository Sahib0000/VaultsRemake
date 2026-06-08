package net.carnagepvp.vaultsremake.data

import net.carnagepvp.vaultsremake.core.database.DatabaseKeyToSave

class VaultData(
    var items: MutableList<VaultItem> = mutableListOf(),
    var iconBase64: String? = null
) : DatabaseKeyToSave<String>()
