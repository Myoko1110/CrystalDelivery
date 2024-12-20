package earth.crystalmc.crystalDelivery.scheduler

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.delivery.DeliveryFailedCode
import earth.crystalmc.crystalDelivery.util.Message
import org.bukkit.scheduler.BukkitRunnable


class DeliveryScheduler : BukkitRunnable() {
    private val deliveryTime = plugin.config.getInt("delivery_time")

    override fun run() {
        val deliveries = Delivery.getDelivered()
        val now = System.currentTimeMillis()

        // TODO: DBへのアクセスが多いので、一度に複数の配達を更新するようにする
        deliveries.forEach {
            if (it.createdAt.time + deliveryTime * 60 < now) {
                val recipientMailbox = Delivery.getDeliveredFromPlayer(it.recipient)
                if (recipientMailbox.size >= 27) {
                    it.apply {
                        failedCode = DeliveryFailedCode.MailboxFull
                        update()
                    }
                    if (it.recipient.isOnline) {
                        it.recipient.player!!.sendMessage(Message.DeliveryFailedMailboxFull.toString())
                    }
                    return@forEach
                }

                it.apply {
                    delivered = true
                    update()
                }
                if (it.recipient.isOnline) {
                    it.recipient.player!!.sendMessage(Message.DeliverySuccess.toString())
                }
            }
        }

        val msg = Message.deliveryCheckSuccess(deliveries.size)
        if (deliveries.isNotEmpty() && msg.isNotEmpty()) {
            plugin.logger.info(msg)
        }
    }
}
