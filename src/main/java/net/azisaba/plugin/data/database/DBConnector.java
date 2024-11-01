package net.azisaba.plugin.data.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.azisaba.plugin.NPCShop;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBConnector {


    protected static HikariDataSource dataSource;

    protected static String shopTable = JavaPlugin.getPlugin(NPCShop.class).getConfig().getString("Database.table");

    public void initialize(@NotNull NPCShop plugin) {
        if (!plugin.getConfig().getBoolean("Database.use", false)) return;
        openConnection(plugin);
        try {
            try (Connection con = dataSource.getConnection()) {
                try (Statement statement = con.createStatement()) {
                    ResultSet shopData = statement.executeQuery("SHOW TABLES LIKE '" + shopTable + "'");
                    if (!shopData.next()) {
                        statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + shopTable +
                                " (" +
                                "name varchar(200) NOT NULL, " +
                                "x int NOT NULL, " +
                                "y int NOT NULL, " +
                                "z int NOT NULL, " +
                                "slot int NOT NULL DEFAULT 0, " +
                                "data MEDIUMBLOB NOT NULL, " +
                                "entity_name varchar(40), " +
                                "entity_type varchar(20), " +
                                "PRIMARY KEY (name, x, y, z, slot)" +
                                ");");
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void openConnection(@NotNull NPCShop plugin) {
        HikariConfig config = new HikariConfig();

        String host = plugin.getConfig().getString("Database.host");
        int port = plugin.getConfig().getInt("Database.port");
        String database = plugin.getConfig().getString("Database.database");
        String username = plugin.getConfig().getString("Database.username");
        String password = plugin.getConfig().getString("Database.password");

        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setMaximumPoolSize(30);
        config.setUsername(username);
        config.setPassword(password);

        dataSource = new HikariDataSource(config);
    }

    public static void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
