package earth.crystalmc.crystalDelivery.delivery

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.utils.Message
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class ReceiveGUI(private val receiver: Player) {
    private var inventory: Inventory = Bukkit.createInventory(null, 27, Message.InventoryPostName.toString())
    private var listener = EventListener()

    private var deliveries: List<Delivery> = Delivery.getDeliveredFromPlayer(receiver)

    init {
        deliveries.forEachIndexed {index, delivery ->
            inventory.setItem(index, delivery.item)
        }
    }

    fun open() {
        plugin.server.pluginManager.registerEvents(listener, plugin)
        receiver.openInventory(inventory)
    }

    fun destroy() {
        HandlerList.unregisterAll(listener)
    }

    private inner class EventListener : Listener {

        @EventHandler
        fun onClick(e: InventoryClickEvent) {
            if (e.rawSlot !in 0..26) {
                if (e.action === InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    e.isCancelled = true
                }
                return;
            }

            if (!arrayOf(InventoryAction.PICKUP_ALL, InventoryAction.MOVE_TO_OTHER_INVENTORY).contains(e.action)) {
                e.isCancelled = true
            } else {
                val currentDelivery = deliveries[e.rawSlot]
                currentDelivery.received = true
                currentDelivery.update()
            }
        }

        @EventHandler
        fun onClose(e: InventoryCloseEvent) {
            if (inventory == e.inventory) {
                destroy()
            }
        }
    }
}
