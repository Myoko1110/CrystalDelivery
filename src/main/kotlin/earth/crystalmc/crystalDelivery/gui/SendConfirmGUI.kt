package earth.crystalmc.crystalDelivery.gui

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.econ
import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.delivery.ShippingFee
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


class SendConfirmGUI(val sender: Player, val recipient: OfflinePlayer, val items: List<ItemStack>) {
    private var inventory: Inventory
    private var listener = EventListener()
    private var fee = ShippingFee.get(items.sumOf { it.amount })
    private var wasSent = false

    init {
        val feeFormat = econ!!.format(fee)
        val currency = if (fee == 1.0) {
            econ!!.currencyNameSingular()
        } else {
            econ!!.currencyNamePlural()
        }

        inventory = Bukkit.createInventory(null, 27, Message.inventoryConfirmName(feeFormat, currency))

        val sendItem = ItemStack(Material.EMERALD_BLOCK)
        val sendItemMeta = sendItem.itemMeta!!
        sendItemMeta.setDisplayName(Message.InventorySendItemName.toString())
        sendItem.setItemMeta(sendItemMeta)
        inventory.setItem(15, sendItem)

        val cancelItem = ItemStack(Material.REDSTONE_BLOCK)
        val cancelItemMeta = cancelItem.itemMeta!!
        cancelItemMeta.setDisplayName(Message.InventoryCancelItemName.toString())
        cancelItem.setItemMeta(cancelItemMeta)
        inventory.setItem(11, cancelItem)

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
                e.isCancelled = true

                when (e.rawSlot) {
                    11 -> {
                        sender.inventory.addItem(*items.toTypedArray())
                        sender.closeInventory()
                    }

                    15 -> {
                        if (econ!!.getBalance(sender) < fee) {
                            sender.sendMessage(Message.SendNoEnoughMoneyError.toString())
                            Message.sendNoEnoughMoneyErrorConsole(sender, recipient)

                        } else {

                            // TODO: DBへのアクセスが多いので、一度に複数の配達を更新するようにする
                            val msg = if (Delivery.save(sender, recipient, items)) {
                                wasSent = true
                                sender.sendMessage(Message.SendSuccess.toString())
                                econ!!.withdrawPlayer(sender, fee)
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

        @EventHandler
        fun onClose(e: InventoryCloseEvent) {
            if (inventory == e.inventory && !wasSent) {
                sender.inventory.addItem(*items.toTypedArray())
                destroy()
            }
        }
    }
}
