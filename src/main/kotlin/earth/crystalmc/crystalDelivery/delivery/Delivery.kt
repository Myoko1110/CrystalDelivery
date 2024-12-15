package earth.crystalmc.crystalDelivery.delivery

import org.bukkit.OfflinePlayer
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import java.sql.Timestamp

class Delivery(
    val id: Int,
    val sender: Player,
    val receiver: OfflinePlayer,
    val item: Array<Item>,
    val createdAt: Timestamp
) {

    fun selializeItem(items: Array<>): String {

    }
}