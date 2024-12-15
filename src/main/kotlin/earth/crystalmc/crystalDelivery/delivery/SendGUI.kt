package earth.crystalmc.crystalDelivery.delivery

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.utils.Message
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class SendGUI(private val sender: Player, private val receiver: OfflinePlayer, private val fee: Double) {
    private var inventory: Inventory = Bukkit.createInventory(null, 18, Message.InventorySendName.toString())
    private var listener = EventListener()

    init {
        val emptyItem = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
        val emptyItemMeta = emptyItem.itemMeta
        emptyItemMeta?.setDisplayName(" ")
        emptyItem.setItemMeta(emptyItemMeta)

        for (i in 10..16) {
            inventory.setItem(i, emptyItem)
        }

        val sendItem = ItemStack(Material.EMERALD_BLOCK)
        val sendItemMeta = sendItem.itemMeta!!
        sendItemMeta.setDisplayName("送る")
        sendItem.setItemMeta(sendItemMeta)
        inventory.setItem(17, sendItem)

        val cancelItem = ItemStack(Material.REDSTONE_BLOCK)
        val cancelItemMeta = cancelItem.itemMeta!!
        cancelItemMeta.setDisplayName("キャンセル")
        cancelItem.setItemMeta(cancelItemMeta)
        inventory.setItem(9, cancelItem)
    }

    fun open() {
        plugin.server.pluginManager.registerEvents(listener, plugin)
        sender.openInventory(inventory)
    }

    fun destroy() {
        HandlerList.unregisterAll(listener)
    }

    private inner class EventListener : Listener {

        @EventHandler
        fun onClick(e: InventoryClickEvent) {
            if (inventory == e.inventory) {
                if (e.rawSlot in 9..17) {
                    e.isCancelled = true

                    when (e.rawSlot) {
                        9 -> {
                            inventory.close()
                        }

                        17 -> {
                            val items = (0..8).mapNotNull { inventory.getItem(it) }
                            if (items.isEmpty()) {
                                sender.sendMessage(Message.InventorySendEmptyError.toString())
                                inventory.close()
                                return
                            }

                            if (Delivery.getDeliveredFromPlayer(receiver).size >= 27) {
                                sender.sendMessage(Message.InventorySendFullError.toString())
                                sender.inventory.addItem(*items.toTypedArray())
                                inventory.close()
                                return
                            }

                            if (Delivery.save(sender, receiver, items)) {
                                sender.sendMessage(Message.SendSuccess.toString())
                            } else {
                                sender.sendMessage(Message.SendError.toString())
                            }
                            inventory.close()
                        }
                    }
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
