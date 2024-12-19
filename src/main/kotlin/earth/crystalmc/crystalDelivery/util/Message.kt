package earth.crystalmc.crystalDelivery.util

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.langData
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player


enum class Message(private val usePrefix: UsePrefix? = null) {
    PluginEnabled,
    NoVault,
    ReloadSuccess(UsePrefix.InfoPrefix),

    CommandPlayerOnly(UsePrefix.ErrorPrefix),
    CommandSyntaxError(UsePrefix.ErrorPrefix),
    CommandNotFound(UsePrefix.ErrorPrefix),
    CommandDenied(UsePrefix.ErrorPrefix),

    InventorySendName,
    InventorySendItemName,
    InventoryCancelItemName,

    InventoryMailboxName,

    SendSuccess(UsePrefix.InfoPrefix),
    SendError(UsePrefix.ErrorPrefix),
    SendEmptyError(UsePrefix.ErrorPrefix),
    SendUnableShulkerError(UsePrefix.ErrorPrefix),
    SendNoEnoughMoneyError(UsePrefix.ErrorPrefix),

    DeliverySuccess(UsePrefix.InfoPrefix),
    DeliveryFailedMailboxFull(UsePrefix.ErrorPrefix),

    JoinDeliveryNotification(UsePrefix.InfoPrefix),
    JoinDeliveryMailboxFullNotification(UsePrefix.ErrorPrefix),

    RedeliverySuccess(UsePrefix.InfoPrefix),
    RedeliveryError(UsePrefix.ErrorPrefix);

    override fun toString(): String {
        var msg = langData.getProperty(name).orEmpty()
        if (msg.isEmpty()) return ""

        usePrefix?.let {
            val prefix = langData.getProperty(it.name).orEmpty()
            msg = "$prefix\u00A7r$msg"
        }
        return replaceColor(msg)
    }

    companion object {
        fun sendSuccessConsole(sender: Player, receiver: OfflinePlayer): String {
            return langData.getProperty("SendSuccessConsole")
                ?.replace("%sender%", sender.name)
                ?.replace("%recipient%", receiver.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun sendErrorConsole(sender: Player, receiver: OfflinePlayer): String {
            return langData.getProperty("SendErrorConsole")
                ?.replace("%sender%", sender.name)
                ?.replace("%recipient%", receiver.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun sendNoEnoughMoneyErrorConsole(sender: Player, receiver: OfflinePlayer): String {
            return langData.getProperty("SendNoEnoughMoneyErrorConsole")
                ?.replace("%sender%", sender.name)
                ?.replace("%recipient%", receiver.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun deliveryCheckSuccess(n: Int): String {
            return langData.getProperty("DeliveryCheckSuccess")
                ?.replace("%n%", n.toString())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun receivedSuccess(receiver: Player, sender: OfflinePlayer, id: Int): String {
            return langData.getProperty("ReceiveSuccess")
                ?.replace("%recipient%", receiver.name)
                ?.replace("%sender%", sender.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.replace("%id%", id.toString())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun receivedError(receiver: Player, sender: OfflinePlayer, id: Int): String {
            return langData.getProperty("ReceiveError")
                ?.replace("%recipient%", receiver.name)
                ?.replace("%sender%", sender.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.replace("%id%", id.toString())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun packingShulkerboxItemName(sender: OfflinePlayer): String {
            return langData.getProperty("PackingShulkerboxItemName")
                ?.replace("%sender%", sender.name ?: langData.getProperty("UnknownPlayer").orEmpty())
                ?.let { replaceColor(it) }.orEmpty()
        }

        fun replaceColor(msg: String): String {
            return msg.replace(
                Regex("#([0-9a-fA-F]{6})"),
                "\u00A7x\u00A7$1\u00A7$2\u00A7$3\u00A7$4\u00A7$5\u00A7$6"
            ).replace(
                Regex("#([0-9a-fA-F]{3})"),
                "\u00A7x\u00A7$1\u00A7$1\u00A7$2\u00A7$2\u00A7$3\u00A7$3"
            ).replace(Regex("&([0-9a-fk-orA-FK-OR])"), "\u00A7$1")
        }
    }
}


enum class UsePrefix {
    InfoPrefix,
    ErrorPrefix,
}
