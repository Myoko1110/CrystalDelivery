package earth.crystalmc.crystalDelivery.utils

import earth.crystalmc.crystalDelivery.CrystalDelivery.Companion.plugin
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.SQLTimeoutException
import java.sql.Statement

class DatabaseManager {
    private lateinit var host: String
    private lateinit var port: String
    private lateinit var db: String
    private lateinit var username: String
    private lateinit var password: String

    private val connection : Connection
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

        val emptyValues = (listOf(_host, _port, _db, _username, _password)).filter{ it.isNullOrEmpty() }
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
        statement.execute("CREATE TABLE IF NOT EXISTS delivery (id INT PRIMARY KEY AUTO_INCREMENT, sender VARCHAR(36), receiver VARCHAR(36), item JSON, created_at DATETIME)")
    }
}