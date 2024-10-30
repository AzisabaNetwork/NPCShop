package net.azisaba.plugin.utils.shop;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class ShopUtil {

    public static boolean isShop(Entity entity) {
        if (entity == null) return false;
        //return entity.getPersistentDataContainer().has(ShopKeys.SHOP_TYPE, PersistentDataType.STRING);
        ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);
        if (mob == null) return false;
        if (mob.getFaction() == null) return false;
        return mob.getFaction().toLowerCase().contains("shop");
    }

    public static int getShopType(@NotNull Entity entity) {
        String string = entity.getPersistentDataContainer().get(ShopKeys.SHOP_TYPE, PersistentDataType.STRING);
        if (string == null) return 0;
        return Integer.parseInt(string);
    }

    public static void setShopType(@NotNull Entity entity, int type) {
        entity.getPersistentDataContainer().set(ShopKeys.SHOP_TYPE, PersistentDataType.STRING, String.valueOf(type));
    }

    public static boolean isShopItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ShopKeys.SHOP_ITEMS, PersistentDataType.STRING);
    }

    public static boolean isShopItemsData(ItemStack item) {
        if (item == null ||!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(ShopKeys.SHOP_ITEM_DATA, PersistentDataType.BOOLEAN);
    }
}
