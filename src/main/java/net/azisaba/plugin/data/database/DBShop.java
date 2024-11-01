package net.azisaba.plugin.data.database;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.azisaba.plugin.npcshop.ShopEntity;
import net.azisaba.plugin.npcshop.ShopLocation;
import net.azisaba.plugin.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DBShop extends DBConnector {

    private static final Multimap<ShopLocation, ItemStack> shopItemsMap = Multimaps.synchronizedListMultimap(ArrayListMultimap.create());

    private static final Map<ShopLocation, ShopEntity> shopEntityMap = new ConcurrentHashMap<>();

    public void delete(Connection con) {
        try {
            boolean delete = false;
            try (PreparedStatement state = con.prepareStatement("SELECT * FROM " + shopTable + ";")) {
                ResultSet set = state.executeQuery();
                if (set.next()) delete = true;
            }

            if (delete) {
                try (PreparedStatement state = con.prepareStatement("DELETE FROM " + shopTable + ";")) {
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
                try (PreparedStatement state = con.prepareStatement("INSERT INTO " + shopTable + " (name, x, y, z, slot, data, entity_name, entity_type) VALUES (?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE data =?, entity_name=?, entity_type=?;")) {

                    ShopEntity shop = shopEntityMap.containsKey(loc) ? shopEntityMap.get(loc) : new ShopEntity(null, null);
                    String t = shop.type() == null ? null : shop.type().name();

                    state.setString(1, loc.w().getName());
                    state.setInt(2, loc.x());
                    state.setInt(3, loc.y());
                    state.setInt(4, loc.z());
                    state.setInt(5, i);
                    state.setBytes(6, item.serializeAsBytes());
                    state.setString(7, shop.name());
                    state.setString(8, t);
                    state.setBytes(9, item.serializeAsBytes());
                    state.setString(10, shop.name());
                    state.setString(11, t);
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
                try (PreparedStatement state = con.prepareStatement("SELECT * FROM " + shopTable + ";")) {
                    ResultSet result = state.executeQuery();
                    while (result.next()) {
                        World w = Bukkit.getWorld(result.getString("name"));
                        if (w == null) continue;
                        int x = result.getInt("x");
                        int y = result.getInt("y");
                        int z = result.getInt("z");
                        ShopLocation loc = new ShopLocation(w, x, y, z);

                        ItemStack item = ItemStack.deserializeBytes(result.getBytes("data"));
                        if (Util.isMythicEnabled()) {
                            String mmid = Util.getMythicID(item);
                            ItemStack mythic = Util.getMythicItemStack(mmid, item.getAmount());
                            if (mythic == null) continue;
                            shopItemsMap.put(loc, Util.dataConvert(mythic, item));
                        } else {
                            shopItemsMap.put(loc, item);
                        }

                        String name = result.getString("entity_name");
                        String type = result.getString("entity_type");
                        shopEntityMap.put(loc, new ShopEntity(name, EntityType.fromName(type)));

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

    public static Map<ShopLocation, ShopEntity> getShopEntity() {
        return shopEntityMap;
    }

    public static boolean itemContains(@NotNull ShopLocation loc) {
        return shopItemsMap.containsKey(loc);
    }

    public static void setShopItems(@NotNull ShopLocation loc, ItemStack add) {
        shopItemsMap.put(loc, add);
        defaultShopEntity(loc);
    }

    public static void setShopEntity(ShopLocation loc, EntityType type, String tag) {
        shopEntityMap.put(loc, new ShopEntity(tag, type));
    }


    public static void replaceShopItem(@NotNull ShopLocation loc, List<ItemStack> list) {
        removeShopItems(loc);
        shopItemsMap.putAll(loc, list);
        defaultShopEntity(loc);
    }


    public static void removeShopItems(@NotNull ShopLocation loc) {
        shopItemsMap.removeAll(loc);
    }

    public static void removeShop(@NotNull ShopLocation loc) {
        shopItemsMap.removeAll(loc);
        shopEntityMap.remove(loc);
    }

    @SuppressWarnings("unused")
    public static void removeShopItems(@NotNull ShopLocation loc, ItemStack item) {
        shopItemsMap.remove(loc, item);
    }

    private static void defaultShopEntity(ShopLocation loc) {
        if (shopEntityMap.containsKey(loc)) return;
        setShopEntity(loc, null, null);
    }

    public static void clear() {
        shopItemsMap.clear();
        shopEntityMap.clear();
    }
}
