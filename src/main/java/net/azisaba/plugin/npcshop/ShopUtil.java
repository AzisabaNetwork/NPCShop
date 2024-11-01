package net.azisaba.plugin.npcshop;

import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import net.azisaba.plugin.utils.Keys;
import net.azisaba.plugin.utils.Util;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class ShopUtil {

    public static boolean isShop(Entity entity) {
        if (entity == null) return false;
        if (Util.isMythicEnabled()) {
            ActiveMob mob = MythicBukkit.inst().getAPIHelper().getMythicMobInstance(entity);
            if (mob == null) return false;
            if (mob.getFaction() == null) return false;
            String f = mob.getFaction().toLowerCase();
            return f.contains("shop") && f.contains("npc");

        } else {
            return entity.getPersistentDataContainer().has(Keys.SHOP_KEEPER, PersistentDataType.STRING);
        }
    }

    public static boolean isShopItemsData(ItemStack item) {
        if (item == null ||!item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(Keys.SHOP_ITEM_DATA, PersistentDataType.BOOLEAN);
    }
}
