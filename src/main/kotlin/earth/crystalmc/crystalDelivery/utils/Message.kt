package earth.crystalmc.crystalDelivery.utils

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.langData

enum class Message {
    PluginEnabled,

    CommandPlayerOnly,
    CommandSyntaxError,
    CommandNotFound,

    InventorySendName,
    InventorySendEmptyError,
    InventorySendFullError,
    InventoryPostName,

    SendSuccess,
    SendError,

    ReceiveSuccess;

    override fun toString(): String {
        return langData.getProperty(name)
    }
}
