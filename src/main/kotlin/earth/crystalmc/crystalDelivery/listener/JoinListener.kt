package earth.crystalmc.crystalDelivery.listener

import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.delivery.DeliveryFailedCode
import earth.crystalmc.crystalDelivery.util.Message
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent


class JoinListener : Listener {
    @EventHandler
    fun onJoin(e: PlayerJoinEvent) {
        val player = e.player

        val deliveries = Delivery.getFromPlayer(player)
        if (deliveries.isNotEmpty()) {

            val message = if (deliveries.any { it.failedCode === DeliveryFailedCode.MailboxFull }) {
                Message.JoinDeliveryMailboxFullNotification
            } else {
                Message.JoinDeliveryNotification
            }
            player.sendMessage(message.toString())
        }
    }
}
