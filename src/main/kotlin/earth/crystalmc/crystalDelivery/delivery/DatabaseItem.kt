package earth.crystalmc.crystalDelivery.delivery

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

private val gson = Gson()

class DatabaseItem(
    val amount: Int,
    val material: String,
    val damage: Int,
    val displayName: String?,
    val itemName: String?,
    val lore: List<String>?,
    val enchantments: Map<String, Int>?,
) {
    fun serializeToJSON(): String {
        return gson.toJson(this)
    }

    fun toItemStack(): ItemStack {
        val item = ItemStack(Material.getMaterial(material)!!, amount)
        val meta = item.itemMeta

        if (displayName !== "") meta.setDisplayName(displayName)
        if (meta is Damageable) meta.damage = damage
        meta.lore = lore
        enchantments?.forEach { (key, value) ->
            meta.addEnchant(Enchantment.getByName(key)!!, value, true)
        }

        item.itemMeta = meta
        return item
    }

    companion object {
        fun deserializeFromJSON(json: String): DatabaseItem {
            val itemType = object : TypeToken<DatabaseItem>() {}.type

            return gson.fromJson(json, itemType)
        }

        fun fromItemStack(item: ItemStack): DatabaseItem {
            val meta = item.itemMeta
            val damage = if (meta is Damageable) meta.damage else 0
            val displayName = meta.displayName
            val lore = meta.lore

            val enchantmentsMap = meta.enchants.mapKeys { it.key.name }.toMap()
            return DatabaseItem(
                item.amount,
                item.type.name,
                damage,
                displayName,
                item.itemMeta.displayName,
                lore,
                enchantmentsMap
            )
        }
    }
}
