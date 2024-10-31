package net.azisaba.plugin.utils;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {

    public static boolean getMythic() {
        return Bukkit.getPluginManager().getPlugin("MythicMobs") != null;
    }

    public static boolean isMythicItem(ItemStack item) {
        if (getMythic()) {
            return MythicBukkit.inst().getItemManager().isMythicItem(item);
        } else {
            if (item == null || !item.hasItemMeta()) return false;
            return item.getItemMeta().getPersistentDataContainer().
                    has(new NamespacedKey("mythicmobs", "type"), PersistentDataType.STRING);
        }
    }

    public static String getMythicID(ItemStack item) {
        if (getMythic()) {
            return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
        } else {
            return item.getItemMeta().getPersistentDataContainer().
                    get(new NamespacedKey("mythicmobs", "type"), PersistentDataType.STRING);
        }
    }

    @Nullable
    public static ItemStack getMythicItemStack(String mmid, int amount) {
        if (getMythic()) {
            return MythicBukkit.inst().getItemManager().getItemStack(mmid, amount);
        } else {
            try {
                return new ItemStack(Material.valueOf(mmid.toUpperCase()));
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
    }

    @SuppressWarnings("unused")
    public static ItemStack getMythicItemStack(String mmid) {
        return getMythicItemStack(mmid, 1);
    }

    @Nullable
    @SuppressWarnings("unused")
    public static MythicItem getMythicItem(String mmid) {
        if (getMythic()) {
            return mmid == null ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        } else {
            return null;
        }
    }

    public static ItemStack toMythic(ItemStack item) {
        if (item == null) return null;
        if (isMythicItem(item)) return item;

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("mythicmobs", "type"),
                PersistentDataType.STRING, item.getType().toString().toLowerCase());
        item.setItemMeta(meta);
        return item;
    }

    @SuppressWarnings("unused")
    public static void toMythic(@NotNull Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                inv.setItem(i, toMythic(item));
            }
        }
    }

    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack dataConvert(@NotNull ItemStack base, @NotNull ItemStack data) {
        ItemMeta baseMeta = base.getItemMeta();
        baseMeta.getPersistentDataContainer().copyTo(data.getItemMeta().getPersistentDataContainer(), true);
        base.setItemMeta(baseMeta);

        return base;
    }
}
