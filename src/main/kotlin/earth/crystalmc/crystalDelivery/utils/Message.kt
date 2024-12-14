package earth.crystalmc.crystalDelivery.utils

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.langData

enum class Message {
    pluginEnabled;

    override fun toString(): String {
        return langData.getProperty(name)
    }
}