package earth.crystalmc.crystalDelivery

import earth.crystalmc.crystalDelivery.command.DeliveryCommandExecutor
import earth.crystalmc.crystalDelivery.listener.JoinListener
import earth.crystalmc.crystalDelivery.scheduler.DeliveryScheduler
import earth.crystalmc.crystalDelivery.util.DatabaseManager
import earth.crystalmc.crystalDelivery.util.Message
import net.milkbowl.vault.economy.Economy
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.RegisteredServiceProvider
import org.bukkit.plugin.java.JavaPlugin
import java.io.*
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.util.*


class CrystalDelivery : JavaPlugin() {

    companion object {
        lateinit var plugin: Plugin private set
        lateinit var langData: Properties private set
        lateinit var databaseManager: DatabaseManager private set

        var econ: Economy? = null

        fun loadMessages() {
            val languageFile = File(plugin.dataFolder, "message.properties")
            if (!languageFile.exists()) {
                var src = plugin.getResource(Locale.getDefault().toString() + ".properties")
                if (src == null) src = plugin.getResource("ja_JP.properties")!!

                try {
                    Files.copy(src, languageFile.toPath())
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            try {
                val inputStreamReader = InputStreamReader(FileInputStream(languageFile), StandardCharsets.UTF_8)
                val bufferedReader = BufferedReader(inputStreamReader)
                langData = Properties()
                langData.load(bufferedReader)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onEnable() {
        plugin = this

        getCommand("delivery")?.setExecutor(DeliveryCommandExecutor())  // command
        saveDefaultConfig()  // config
        loadMessages()  // messages

        if (!setupEconomy()) {
            this.logger.severe(Message.NoVault.toString())
            server.pluginManager.disablePlugin(this)
            return
        }

        databaseManager = DatabaseManager()  // database
        server.pluginManager.registerEvents(JoinListener(), this)  // listener
        DeliveryScheduler().runTaskTimer(this, 0, 20 * 60)  // scheduler

        this.logger.info(Message.PluginEnabled.toString())
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

    private fun setupEconomy(): Boolean {
        if (server.pluginManager.getPlugin("Vault") == null) {
            return false
        }
        val rsp: RegisteredServiceProvider<Economy> =
            server.servicesManager.getRegistration(
                Economy::class.java
            ) ?: return false
        econ = rsp.provider
        return econ != null
    }

}
