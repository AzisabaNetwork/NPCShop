package net.azisaba.plugin.database;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.azisaba.plugin.utils.MythicUtil;
import net.azisaba.plugin.utils.shop.ShopLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class DBShop extends DBConnector {

    private static final Multimap<ShopLocation, ItemStack> shopItemsMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    public void delete(Connection con) {
        try {
            boolean delete = false;
            try (PreparedStatement state = con.prepareStatement("SELECT * FROM " + shopTable)) {
                ResultSet set = state.executeQuery();
                if (set.next()) delete = true;
            }

            if (delete) {
                try (PreparedStatement state = con.prepareStatement("DELETE FROM " + shopTable)) {
                    state.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void set(Connection con, @NotNull ShopLocation loc, @NotNull List<ItemStack> list) {
        try {
            int i = 0;
            for (ItemStack item : list) {
                if (item == null || item.getType().isAir() || !item.hasItemMeta()) continue;
                try (PreparedStatement state = con.prepareStatement("INSERT INTO " + shopTable + " (name, x, y, z, slot, data) VALUES (?,?,?,?,?,?) ON DUPLICATE KEY UPDATE data =?")) {

                    state.setString(1, loc.w().getName());
                    state.setInt(2, loc.x());
                    state.setInt(3, loc.y());
                    state.setInt(4, loc.z());
                    state.setInt(5, i);
                    state.setBytes(6, item.serializeAsBytes());
                    state.setBytes(7, item.serializeAsBytes());
                    state.executeUpdate();
                    i++;
                }

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void load() {
        try {
            try (Connection con = dataSource.getConnection()) {
                try (PreparedStatement state = con.prepareStatement("SELECT * FROM " + shopTable)) {
                    ResultSet result = state.executeQuery();
                    while (result.next()) {
                        World w = Bukkit.getWorld(result.getString("name"));
                        int x = result.getInt("x");
                        int y = result.getInt("y");
                        int z = result.getInt("z");
                        if (w == null) continue;
                        ShopLocation loc = new ShopLocation(w, x, y, z);

                        ItemStack item = ItemStack.deserializeBytes(result.getBytes("data"));
                        String mmid = MythicUtil.getMythicID(item);
                        ItemStack mythic = MythicUtil.getMythicItemStack(mmid, item.getAmount());

                        shopItemsMap.put(loc, mythic);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static Multimap<ShopLocation, ItemStack> getShopItems() {
        return shopItemsMap;
    }

    public static boolean contains(@NotNull Location loc) {
        for (ShopLocation record : getShopItems().keySet()) {
            if (record.isSimilar(loc)) return true;
        }
        return false;
    }

    public static void setShopItems(@NotNull ShopLocation loc, ItemStack add) {
        shopItemsMap.put(loc, add);
    }

    public static void replaceShopItem(@NotNull ShopLocation loc, List<ItemStack> list) {
        removeShopItems(loc);
        shopItemsMap.putAll(loc, list);
    }

    public static void removeShopItems(@NotNull ShopLocation loc) {
        shopItemsMap.removeAll(loc);
    }

    public static void removeShopItems(@NotNull ShopLocation loc, ItemStack item) {
        shopItemsMap.remove(loc, item);}
}
