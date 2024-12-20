package earth.crystalmc.crystalDelivery.util

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import earth.crystalmc.crystalDelivery.delivery.Delivery
import earth.crystalmc.crystalDelivery.delivery.DeliveryFailedCode
import org.bukkit.OfflinePlayer
import java.sql.*


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
        statement.execute("CREATE TABLE IF NOT EXISTS delivery (id INT PRIMARY KEY AUTO_INCREMENT, sender VARCHAR(36), recipient VARCHAR(36), item JSON, delivered BOOLEAN DEFAULT FALSE, received BOOLEAN DEFAULT FALSE, failed_code INTEGER DEFAULT NULL, created_at DATETIME)")
    }

    fun addDelivery(sender: String, recipient: String, item: String, createdAt: Timestamp): Boolean {
        try {
            val statement =
                connection.prepareStatement("INSERT INTO delivery (sender, recipient, item, created_at) VALUES (?, ?, ?, ?)")
            statement.setString(1, sender)
            statement.setString(2, recipient)
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
        val resultSet =
            statement.executeQuery("SELECT * FROM delivery WHERE delivered = FALSE AND received = FALSE AND failed_code IS NULL")

        return Delivery.deserializeFromDatabase(resultSet)
    }

    fun getDeliveriesFromPlayer(player: OfflinePlayer): List<Delivery> {
        val statement =
            connection.prepareStatement("SELECT * FROM delivery WHERE (delivered = TRUE AND received = FALSE) OR (delivered = FALSE AND received = FALSE AND failed_code IS NOT NULL) AND recipient = ?")
        statement.setString(1, player.uniqueId.toString())

        val resultSet = statement.executeQuery()
        return Delivery.deserializeFromDatabase(resultSet)
    }

    fun getValidDeliveriesFromPlayer(player: OfflinePlayer): List<Delivery> {
        val statement =
            connection.prepareStatement("SELECT * FROM delivery WHERE delivered = TRUE AND received = FALSE AND failed_code IS NULL AND recipient = ?")
        statement.setString(1, player.uniqueId.toString())

        val resultSet = statement.executeQuery()
        return Delivery.deserializeFromDatabase(resultSet)
    }

    fun getFailedDeliveriesFromPlayer(player: OfflinePlayer): List<Delivery> {
        val statement =
            connection.prepareStatement("SELECT * FROM delivery WHERE delivered = FALSE AND received = FALSE AND failed_code IS NOT NULL AND recipient = ?")
        statement.setString(1, player.uniqueId.toString())

        val resultSet = statement.executeQuery()
        return Delivery.deserializeFromDatabase(resultSet)
    }

    fun updateDelivery(
        id: Int,
        delivered: Boolean,
        received: Boolean,
        failedCode: DeliveryFailedCode? = null
    ): Boolean {
        try {
            val statement =
                connection.prepareStatement("UPDATE delivery SET delivered = ?, received = ?, failed_code = ? WHERE id = ?")
            statement.setBoolean(1, delivered)
            statement.setBoolean(2, received)
            if (failedCode == null) {
                statement.setObject(3, null)
            } else {
                statement.setInt(3, failedCode.code)
            }

            statement.setInt(4, id)

            return statement.executeUpdate() > 0
        } catch (e: SQLException) {
            e.printStackTrace()
            return false
        }
    }
}
