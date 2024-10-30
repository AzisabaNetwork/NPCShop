package net.azisaba.plugin.utils;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class MythicUtil {

    public static boolean isMythicItem(ItemStack item) {
        return MythicBukkit.inst().getItemManager().isMythicItem(item);
    }

    public static String getMythicID(ItemStack item) {
        return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
    }

    public static ItemStack getMythicItemStack(String mmid, int amount) {
        return MythicBukkit.inst().getItemManager().getItemStack(mmid, amount);
    }

    public static ItemStack getMythicItemStack(String mmid) {
        return getMythicItemStack(mmid, 1);
    }

    public static MythicItem getMythicItem(String mmid) {
        return mmid == null ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
    }

    public static ItemStack toMythic(ItemStack item) {
        if (item == null) return null;
        if (isMythicItem(item)) return item;

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey("mythicmobs", "type")
                , PersistentDataType.STRING, item.getType().toString().toLowerCase());
        item.setItemMeta(meta);
        return item;
    }

    public static void toMythic(@NotNull Inventory inv) {
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item != null) {
                inv.setItem(i, toMythic(item));
            }
        }
    }
}
