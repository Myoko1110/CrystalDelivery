package earth.crystalmc.crystalDelivery.utils

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.DatabaseItem
import earth.crystalmc.crystalDelivery.delivery.Delivery
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import java.sql.*
import java.util.*

class DatabaseManager {
    private lateinit var host: String
    private lateinit var port: String
    private lateinit var db: String
    private lateinit var username: String
    private lateinit var password: String

    private val connection: Connection
        get() {
            var con: Connection? = null
            try {
                con = DriverManager.getConnection("jdbc:mysql://$host:$port/$db", username, password)
            } catch (e: SQLException) {
                e.printStackTrace()
            }

            return con!!
        }

    init {
        initialize()
    }

    fun initialize() {
        reloadSettings()
        initializeDatabase()
        initializeTable()
    }

    private fun reloadSettings() {
        val section = plugin.config.getConfigurationSection("database")!!

        val _host = section.getString("host")
        val _port = section.getString("port")
        val _db = section.getString("db")
        val _username = section.getString("username")
        val _password = section.getString("password")

        val emptyValues = (listOf(_host, _port, _db, _username, _password)).filter { it.isNullOrEmpty() }
        if (emptyValues.isNotEmpty()) {
            throw SQLException("データベースの設定を完了させてください")
        }

        host = _host!!
        port = _port!!
        db = _db!!
        username = _username!!
        password = _password!!
    }

    private fun initializeDatabase() {
        val statement: Statement
        try {
            statement = DriverManager.getConnection("jdbc:mysql://$host:$port", username, password).createStatement()
        } catch (e: SQLException) {
            throw SQLException("データベースに接続できませんでした")
        } catch (e: SQLTimeoutException) {
            throw SQLException("データベースに接続できませんでした")
        }

        // データベース作成
        statement.execute("CREATE DATABASE IF NOT EXISTS $db")
    }

    private fun initializeTable() {
        val statement = connection.createStatement()

        // テーブル作成
        statement.execute("CREATE TABLE IF NOT EXISTS delivery (id INT PRIMARY KEY AUTO_INCREMENT, sender VARCHAR(36), receiver VARCHAR(36), item JSON, delivered BOOLEAN DEFAULT FALSE, received BOOLEAN DEFAULT FALSE, created_at DATETIME)")
    }

    fun addDelivery(sender: String, receiver: String, item: String, createdAt: Timestamp): Boolean {
        try {
            val statement =
                connection.prepareStatement("INSERT INTO delivery (sender, receiver, item, created_at) VALUES (?, ?, ?, ?)")
            statement.setString(1, sender)
            statement.setString(2, receiver)
            statement.setString(3, item)
            statement.setTimestamp(4, createdAt)

            return statement.executeUpdate() > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }

    fun getValidDeliveries(): List<Delivery> {
        val statement = connection.createStatement()
        val resultSet = statement.executeQuery("SELECT * FROM delivery WHERE delivered = FALSE AND received = FALSE")

        val deliveries = mutableListOf<Delivery>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val sender = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("sender")))
            val receiver = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("receiver")))
            val item = DatabaseItem.deserializeFromJSON(resultSet.getString("item")).toItemStack()
            val delivered = resultSet.getBoolean("delivered")
            val received = resultSet.getBoolean("received")
            val createdAt = resultSet.getTimestamp("created_at")

            deliveries.add(Delivery(id, sender, receiver, item, delivered, received, createdAt))
        }

        return deliveries
    }

    fun getValidDeliveriesFromPlayer(player: OfflinePlayer): List<Delivery> {
        val statement = connection.prepareStatement("SELECT * FROM delivery WHERE delivered = TRUE AND received = FALSE AND receiver = ?")
        statement.setString(1, player.uniqueId.toString())

        val resultSet = statement.executeQuery()
        val deliveries = mutableListOf<Delivery>()

        while (resultSet.next()) {
            val id = resultSet.getInt("id")
            val sender = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("sender")))
            val receiver = Bukkit.getOfflinePlayer(UUID.fromString(resultSet.getString("receiver")))
            val item = DatabaseItem.deserializeFromJSON(resultSet.getString("item")).toItemStack()
            val delivered = resultSet.getBoolean("delivered")
            val received = resultSet.getBoolean("received")
            val createdAt = resultSet.getTimestamp("created_at")

            deliveries.add(Delivery(id, sender, receiver, item, delivered, received, createdAt))
        }

        return deliveries
    }

    fun updateDelivery(id: Int, delivered: Boolean, received: Boolean): Boolean {
        try {
            val statement = connection.prepareStatement("UPDATE delivery SET delivered = ?, received = ? WHERE id = ?")
            statement.setBoolean(1, delivered)
            statement.setBoolean(2, received)
            statement.setInt(3, id)

            return statement.executeUpdate() > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }
}
