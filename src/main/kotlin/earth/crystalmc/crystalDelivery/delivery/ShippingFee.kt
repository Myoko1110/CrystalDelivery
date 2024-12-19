package earth.crystalmc.crystalDelivery.delivery

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin

class ShippingFee {
    companion object {
        val shippingFeeType = ShippingFeeType.valueOf(plugin.config.getString("shipping_fee.type")!!)
        private val fee = plugin.config.getDouble("shipping_fee.fee")
        val perItem = plugin.config.getDouble("shipping_fee.per_item")
        val fixed = plugin.config.getDouble("shipping_fee.fixed")
        val limit = plugin.config.getInt("shipping_fee.limit")

        fun get(num: Int): Double {
            return when (shippingFeeType) {
                ShippingFeeType.Flat -> fee
                ShippingFeeType.FixedWithLimit -> if (num <= limit) fixed else fixed + ((num - limit) * perItem)
                ShippingFeeType.PerItem -> fixed + (num * perItem)
            }
        }
    }
}