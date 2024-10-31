package net.azisaba.plugin.data;

import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.data.database.DBConnector;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.npcshop.ShopLocation;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class SaveDB extends DBConnector {

    public void save(boolean force) {
        saveShops(force, false);
    }

    public void saveShops(boolean force, boolean delete) {
        NPCShop plugin = JavaPlugin.getPlugin(NPCShop.class);
        if (!plugin.getConfig().getBoolean("Database.use", false)) return;

        if (force) saveShopApply(delete);
        else plugin.runAsync(()-> saveShopApply(delete));
    }

    private void saveShopApply(boolean delete) {
        try {
            long time = System.currentTimeMillis();
            try (Connection con = dataSource.getConnection()) {
                if (delete) new DBShop().delete(con);
                for (ShopLocation ab : DBShop.getShopItems().keySet()) {
                    List<ItemStack> list = DBShop.getShopItems().get(ab).stream().toList();
                    new DBShop().set(con, ab, list);
                }
            }
            time = System.currentTimeMillis() - time;
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (!p.hasPermission("npcshop.sql.notifications")) continue;
                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&fショップ情報: &b" + time + "ms"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
