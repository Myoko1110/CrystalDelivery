package earth.crystalmc.crystalDelivery.delivery

import com.google.gson.Gson
import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.databaseManager
import earth.crystalmc.crystalDelivery.util.DatabaseItem
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.*


private val gson = Gson()

class Delivery(
    val id: Int,
    val sender: OfflinePlayer,
    val recipient: OfflinePlayer,
    val items: List<ItemStack>,
    var delivered: Boolean,
    var received: Boolean,
    var failedCode: DeliveryFailedCode?,
    val createdAt: Timestamp
) {
    /**
     * デリバリー情報を更新します（delivered, received、failedCodeのみ）
     */
    fun update(): Boolean {
        return databaseManager.updateDelivery(id, delivered, received, failedCode)
    }

    companion object {
        fun save(sender: Player, recipient: OfflinePlayer, item: List<ItemStack>): Boolean {
            val senderUUID = sender.uniqueId.toString()
            val recipientUUID = recipient.uniqueId.toString()
            val now = Timestamp(System.currentTimeMillis())

            val result = mutableListOf<Boolean>()
            val json = gson.toJson(item.map { DatabaseItem.fromItemStack(it) })
            val res = databaseManager.addDelivery(senderUUID, recipientUUID, json, now)
            result.add(res)

            return !result.contains(false)
        }

        fun getDelivered(): List<Delivery> {
            return databaseManager.getValidDeliveries()
        }

        fun getFromPlayer(player: OfflinePlayer): List<Delivery> {
            return databaseManager.getDeliveriesFromPlayer(player)
        }

        fun getDeliveredFromPlayer(player: OfflinePlayer): List<Delivery> {
            return databaseManager.getValidDeliveriesFromPlayer(player)
        }

        fun getFailedFromPlayer(player: OfflinePlayer): List<Delivery> {
            return databaseManager.getFailedDeliveriesFromPlayer(player)
        }

        fun deserializeFromDatabase(resultSet: ResultSet): MutableList<Delivery> {
            val deliveries = mutableListOf<Delivery>()

            while (resultSet.next()) {
                deliveries.add(
                    Delivery(
                        id = resultSet.getInt("id"),
                        sender = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("sender"))),
                        recipient = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("recipient"))),
                        items = DatabaseItem.deserializeFromJSON(resultSet.getString("item")).map { it.toItemStack() },
                        delivered = resultSet.getBoolean("delivered"),
                        received = resultSet.getBoolean("received"),
                        failedCode = DeliveryFailedCode.byCode(resultSet.getInt("failed_code")),
                        createdAt = resultSet.getTimestamp("created_at")
                    )
                )
            }

            return deliveries
        }
    }
}
