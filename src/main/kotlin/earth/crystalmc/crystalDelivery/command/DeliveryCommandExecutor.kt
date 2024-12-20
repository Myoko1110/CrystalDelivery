package earth.crystalmc.crystalDelivery.command

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.databaseManager
import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.loadMessages
import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.gui.ReceiveGUI
import earth.crystalmc.crystalDelivery.gui.SendGUI
import earth.crystalmc.crystalDelivery.util.Message
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player


class DeliveryCommandExecutor : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(Message.CommandSyntaxError.toString())
            return false
        }

        when (args[0]) {
            "send" -> {
                if (args.size < 2) {
                    sender.sendMessage(Message.CommandSyntaxError.toString())
                    return false
                }
                if (sender !is Player) {
                    sender.sendMessage(Message.CommandPlayerOnly.toString())
                    return false
                }
                if (!sender.hasPermission("crystaldelivery.send")) {
                    sender.sendMessage(Message.CommandDenied.toString())
                    return false
                }

                val receiver = Bukkit.getOfflinePlayer(args[1])

                SendGUI(sender, receiver).open()
                return true
            }

            "post" -> {
                if (sender !is Player) {
                    sender.sendMessage(Message.CommandPlayerOnly.toString())
                    return false
                }
                if (!sender.hasPermission("crystaldelivery.post")) {
                    sender.sendMessage(Message.CommandDenied.toString())
                    return false
                }

                ReceiveGUI(sender).open()
                return true
            }

            "redelivery" -> {
                if (sender !is Player) {
                    sender.sendMessage(Message.CommandPlayerOnly.toString())
                    return false
                }
                if (!sender.hasPermission("crystaldelivery.redelivery")) {
                    sender.sendMessage(Message.CommandDenied.toString())
                    return false
                }

                val results = Delivery.getFailedFromPlayer(sender).map {
                    it.failedCode = null
                    it.update()
                }
                if (results.any { !it }) {
                    sender.sendMessage(Message.RedeliveryError.toString())
                    return false
                }

                sender.sendMessage(Message.RedeliverySuccess.toString())
            }

            "reload" -> {
                if (!sender.hasPermission("crystaldelivery.reload")) {
                    sender.sendMessage(Message.CommandDenied.toString())
                    return false
                }

                databaseManager.initialize()
                loadMessages()
                plugin.saveDefaultConfig()

                sender.sendMessage(Message.ReloadSuccess.toString())
                return true
            }

            else -> {
                sender.sendMessage(Message.CommandNotFound.toString())
            }
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        return when {
            args.size == 1 -> listOf(
                "send",
                "post",
                "redelivery",
                "reload"
            ).filter { sender.hasPermission("crystaldelivery.$it") }.toMutableList()

            args.size == 2 && args[0] == "send" -> Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
            else -> mutableListOf()
        }
    }
}
