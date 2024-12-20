package earth.crystalmc.crystalDelivery.util

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable


class DatabaseItem(
    private val amount: Int,
    private val material: String,
    private val damage: Int,
    private val displayName: String?,
    private val itemName: String?,
    private val lore: List<String>?,
    private val enchantments: Map<String, Int>?,
) {

    fun toItemStack(): ItemStack {
        val item = ItemStack(Material.getMaterial(material)!!, amount)
        val meta = item.itemMeta!!

        if (displayName !== "") meta.setDisplayName(displayName)
        if (itemName !== "") meta.setItemName(itemName)
        if (meta is Damageable) meta.damage = damage
        meta.lore = lore
        enchantments?.forEach { (key, value) ->
            meta.addEnchant(Enchantment.getByName(key)!!, value, true)
        }

        item.itemMeta = meta
        return item
    }

    companion object {
        private val gson = Gson()

        fun deserializeFromJSON(json: String): List<DatabaseItem> {
            val itemType = object : TypeToken<List<DatabaseItem>>() {}.type

            return gson.fromJson(json, itemType)
        }

        fun fromItemStack(item: ItemStack): DatabaseItem {
            val meta = item.itemMeta!!
            val damage = if (meta is Damageable) meta.damage else 0
            val displayName = meta.displayName
            val lore = meta.lore

            val enchantmentsMap = meta.enchants.mapKeys { it.key.name }.toMap()
            return DatabaseItem(
                item.amount,
                item.type.name,
                damage,
                displayName,
                meta.displayName,
                lore,
                enchantmentsMap
            )
        }
    }
}
