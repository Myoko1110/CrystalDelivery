package earth.crystalmc.crystalDelivery.gui

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.util.Message
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

val shulkerBoxes = mutableListOf(
    Material.SHULKER_BOX,
    Material.WHITE_SHULKER_BOX,
    Material.LIGHT_GRAY_SHULKER_BOX,
    Material.GRAY_SHULKER_BOX,
    Material.BLACK_SHULKER_BOX,
    Material.BROWN_SHULKER_BOX,
    Material.RED_SHULKER_BOX,
    Material.ORANGE_SHULKER_BOX,
    Material.YELLOW_SHULKER_BOX,
    Material.LIME_SHULKER_BOX,
    Material.GREEN_SHULKER_BOX,
    Material.CYAN_SHULKER_BOX,
    Material.LIGHT_BLUE_SHULKER_BOX,
    Material.BLUE_SHULKER_BOX,
    Material.PURPLE_SHULKER_BOX,
    Material.MAGENTA_SHULKER_BOX,
    Material.PINK_SHULKER_BOX
)

class SendGUI(private val sender: Player, private val recipient: OfflinePlayer) {
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
        sendItemMeta.setDisplayName(Message.InventorySendItemName.toString())
        sendItem.setItemMeta(sendItemMeta)
        inventory.setItem(17, sendItem)

        val cancelItem = ItemStack(Material.REDSTONE_BLOCK)
        val cancelItemMeta = cancelItem.itemMeta!!
        cancelItemMeta.setDisplayName(Message.InventoryCancelItemName.toString())
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
                            sender.closeInventory()
                        }

                        17 -> {
                            // 配達するアイテムが指定されていない場合
                            val items = (0..8).mapNotNull { inventory.getItem(it) }
                            if (items.isEmpty()) {
                                sender.sendMessage(Message.SendEmptyError.toString())

//                                // ポストが満杯だった場合
//                            } else if (Delivery.getDeliveredFromPlayer(recipient).size >= 27) {
//                                sender.sendMessage(Message.InventorySendFullError.toString())
//                                sender.inventory.addItem(*items.toTypedArray())

                            } else if (items.any { shulkerBoxes.contains(it.type) }) {
                                sender.sendMessage(Message.SendUnableShulkerError.toString())
                                sender.inventory.addItem(*items.toTypedArray())

                            } else {
                                // TODO: DBへのアクセスが多いので、一度に複数の配達を更新するようにする
                                val msg = if (Delivery.save(sender, recipient, items)) {
                                    sender.sendMessage(Message.SendSuccess.toString())
                                    Message.sendSuccessConsole(sender, recipient)
                                } else {
                                    sender.sendMessage(Message.SendError.toString())
                                    Message.sendErrorConsole(sender, recipient)
                                }
                                if (msg.isNotEmpty()) {
                                    plugin.logger.info(msg)
                                }
                            }

                            sender.closeInventory()
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
