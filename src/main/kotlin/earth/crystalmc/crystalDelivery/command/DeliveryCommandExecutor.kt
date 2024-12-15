package earth.crystalmc.crystalDelivery.command

import earth.crystalmc.crystalDelivery.delivery.ReceiveGUI
import earth.crystalmc.crystalDelivery.delivery.SendGUI
import earth.crystalmc.crystalDelivery.utils.Message
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DeliveryCommandExecutor : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Message.CommandPlayerOnly.toString())
            return false
        }
        if (args.isEmpty()) {
            sender.sendMessage(Message.CommandSyntaxError.toString())
            return false
        }

        when (args[0]) {
            "send" -> {
                val receiver = Bukkit.getOfflinePlayer(args[1])

                SendGUI(sender, receiver, 0.0).open()
                return true
            }

            "post" -> {
                ReceiveGUI(sender).open()
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
        if (args.size == 1) {
            return mutableListOf("send", "post")
        } else if (args.size == 2 && args[0] == "send") {
            return Bukkit.getOnlinePlayers().map { it.name }.toMutableList()
        }
        return mutableListOf()
    }
}
