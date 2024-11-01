package net.azisaba.plugin.utils;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.items.MythicItem;
import net.azisaba.plugin.NPCShop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Util {

    public static boolean isMythicEnabled() {
        return isMythic() && isEnabled();
    }

    public static boolean isMythic() {
        Plugin p = Bukkit.getPluginManager().getPlugin("MythicMobs");
        return p != null && p.isEnabled();
    }

    public static boolean isEnabled() {
        return JavaPlugin.getPlugin(NPCShop.class).getConfig().getBoolean("EntityOptions.UseMythicMobs", false);
    }

    public static boolean isMythicItem(ItemStack item) {
        if (isMythicEnabled()) {
            return MythicBukkit.inst().getItemManager().isMythicItem(item);
        } else {
            if (item == null || !item.hasItemMeta()) return false;
            return item.getItemMeta().getPersistentDataContainer().
                    has(Keys.SHOP_MYTHIC, PersistentDataType.STRING);
        }
    }

    public static String getMythicID(ItemStack item) {
        if (isMythicEnabled()) {
            return MythicBukkit.inst().getItemManager().getMythicTypeFromItem(item);
        } else {
            return item.getItemMeta().getPersistentDataContainer().
                    get(Keys.SHOP_MYTHIC, PersistentDataType.STRING);
        }
    }

    @Nullable
    public static ItemStack getMythicItemStack(String mmid, int amount) {
        if (isMythicEnabled()) {
            return MythicBukkit.inst().getItemManager().getItemStack(mmid, amount);
        } else {
            try {
                return new ItemStack(Material.valueOf(mmid.toUpperCase()), amount);
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
        if (isMythicEnabled()) {
            return mmid == null ? null : MythicBukkit.inst().getItemManager().getItem(mmid).orElse(null);
        } else {
            return null;
        }
    }

    public static ItemStack toMythic(ItemStack item) {
        if (item == null) return null;
        if (isMythicItem(item)) return item;

        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(Keys.SHOP_MYTHIC,
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
