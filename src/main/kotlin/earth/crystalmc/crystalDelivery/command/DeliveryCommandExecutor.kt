package earth.crystalmc.crystalDelivery.command

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class DeliveryCommandExecutor : CommandExecutor, TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage("プレイヤーのみ実行可能です")
            return false
        }
        if (args.isEmpty()) {
            sender.sendMessage("引数が不足しています")
            return false
        }

        when (args[0]) {
            "send" -> {
                sender.sendMessage("create")
                return true
            }
            else -> {
                sender.sendMessage("存在しない引数です")
            }
        }
        return false
    }

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String> {
        return mutableListOf("send")
    }
}