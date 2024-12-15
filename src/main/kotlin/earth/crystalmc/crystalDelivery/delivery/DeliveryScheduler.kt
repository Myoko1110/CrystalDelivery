package earth.crystalmc.crystalDelivery.delivery

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.utils.Message
import org.bukkit.scheduler.BukkitRunnable

class DeliveryScheduler : BukkitRunnable() {
    val deliveryTime = plugin.config.getInt("delivery_time")

    override fun run() {
        val deliveries = Delivery.getDelivered()
        val now = System.currentTimeMillis()

        deliveries.forEach {
            if (it.createdAt.time + deliveryTime * 60 < now) {
                it.delivered = true
                it.update()

                if (it.receiver.isOnline) {
                    it.receiver.player!!.sendMessage(Message.ReceiveSuccess.toString())
                }
            }
        }
    }
}
