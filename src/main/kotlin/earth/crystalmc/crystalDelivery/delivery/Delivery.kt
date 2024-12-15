package earth.crystalmc.crystalDelivery.delivery

import com.google.gson.Gson
import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.databaseManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.sql.Timestamp

private val gson = Gson()

class Delivery(
    val id: Int,
    val sender: OfflinePlayer,
    val receiver: OfflinePlayer,
    val item: ItemStack,
    var delivered: Boolean,
    var received: Boolean,
    val createdAt: Timestamp
) {
    /**
     * デリバリー情報を更新します（delivered, receivedのみ）
     */
    fun update() {
        databaseManager.updateDelivery(id, delivered, received)
    }

    companion object {
        fun save(sender: Player, receiver: OfflinePlayer, item: List<ItemStack>): Boolean {
            val senderUUID = sender.uniqueId.toString()
            val receiverUUID = receiver.uniqueId.toString()
            val now = Timestamp(System.currentTimeMillis())

            val result = mutableListOf<Boolean>()
            item.forEach {
                val json = gson.toJson(DatabaseItem.fromItemStack(it))
                val res = databaseManager.addDelivery(senderUUID, receiverUUID, json, now)
                result.add(res)
            }

            return !result.contains(false)
        }

        fun getDelivered(): List<Delivery> {
            return databaseManager.getValidDeliveries()
        }

        fun getDeliveredFromPlayer(player: OfflinePlayer): List<Delivery> {
            return databaseManager.getValidDeliveriesFromPlayer(player)
        }
    }
}
