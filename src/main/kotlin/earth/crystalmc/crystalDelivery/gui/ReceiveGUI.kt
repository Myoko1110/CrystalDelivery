package earth.crystalmc.crystalDelivery.gui

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.util.Message
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta


class ReceiveGUI(private val recipient: Player) {
    private var inventory: Inventory = Bukkit.createInventory(null, 27, Message.InventoryMailboxName.toString())
    private var listener = EventListener()

    private var deliveries: List<Delivery> = Delivery.getDeliveredFromPlayer(recipient)

    init {
        deliveries.take(27).forEachIndexed { index, delivery ->

            val shulkerBox = ItemStack(Material.BROWN_SHULKER_BOX)
            val meta = shulkerBox.itemMeta!! as BlockStateMeta

            meta.setDisplayName(Message.packingShulkerboxItemName(delivery.sender))

            val box = meta.blockState as ShulkerBox
            val boxInventory = box.inventory
            boxInventory.contents = delivery.items.toTypedArray()

            meta.blockState = box
            shulkerBox.itemMeta = meta
            inventory.setItem(index, shulkerBox)
        }

    }

    fun open() {
        plugin.server.pluginManager.registerEvents(listener, plugin)
        recipient.openInventory(inventory)
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
                return
            }

            if (!arrayOf(InventoryAction.PICKUP_ALL, InventoryAction.MOVE_TO_OTHER_INVENTORY).contains(e.action)) {
                e.isCancelled = true
            } else {

                val currentDelivery = deliveries[e.rawSlot].apply { received = true }
                val msg = if (currentDelivery.update()) {
                    Message.receivedSuccess(recipient, currentDelivery.sender, currentDelivery.id)
                } else {
                    Message.receivedError(recipient, currentDelivery.sender, currentDelivery.id)
                }
                if (msg.isNotEmpty()) {
                    plugin.logger.info(msg)
                }
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
