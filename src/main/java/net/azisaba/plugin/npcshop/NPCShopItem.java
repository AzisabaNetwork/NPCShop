package net.azisaba.plugin.npcshop;

import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.utils.Util;
import net.azisaba.plugin.utils.Keys;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class NPCShopItem {

    @Nullable
    protected static ItemStack getMythicItem(@NotNull String mmidAndAmount) {
        if (!mmidAndAmount.contains(" ")) return null;
        String[] split = mmidAndAmount.split(" ");
        String mmid = split[0];
        int amount = Integer.parseInt(split[1]);
        return Util.getMythicItemStack(mmid, amount);
    }

    @Nullable
    protected static String setMythicItem(@NotNull ItemStack mythic) {
        if (!Util.isMythicItem(mythic)) return null;
        String mmid = Util.getMythicID(mythic);
        return mmid + " " + mythic.getAmount();
    }

    public record Deserializer(ItemStack item, List<ItemStack> list)  {

        public Deserializer(@NotNull ItemStack item, @NotNull List<ItemStack> list) {
            int i = 0;
            ItemMeta meta = item.getItemMeta();
            while (i < 100) {
                NamespacedKey key = new NamespacedKey("npcshop", "recipe_" + i);
                if (meta.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                    String s = meta.getPersistentDataContainer().get(key, PersistentDataType.STRING);
                    if (s != null) {
                        ItemStack stack = NPCShopItem.getMythicItem(s);
                        if (stack == null) continue;
                        list.add(stack);
                    }
                    i++;
                } else {
                    break;
                }
            }
            this.item = item;
            this.list = list;
        }
    }

    public record Serializer(ItemStack item, List<ItemStack> list) {

        private static final int max = JavaPlugin.getPlugin(NPCShop.class).getConfig().getInt("ShopOptions.MaxStackPerItem", 20);

        public Serializer(@NotNull ItemStack item, @NotNull List<ItemStack> list) {
            ItemMeta meta = item.getItemMeta();
            for (ItemStack is : list) {
                if (is == null) continue;
                if (!Util.isMythicItem(is)) continue;
                int i = 0;
                setData(meta.getPersistentDataContainer(), i, is);
            }
            if (!meta.getPersistentDataContainer().has(Keys.SHOP_ITEM_DATA, PersistentDataType.BOOLEAN)) {
                meta.getPersistentDataContainer().set(Keys.SHOP_ITEM_DATA, PersistentDataType.BOOLEAN, true);
            }
            item.setItemMeta(meta);
            this.item = item;
            this.list = list;
        }

        private void setData(@NotNull PersistentDataContainer pc, int i, ItemStack item) {
            if (i == max) return;
            NamespacedKey key = new NamespacedKey("npcshop", "recipe_" + i);
            if (pc.has(key, PersistentDataType.STRING)) {
                i++;
                setData(pc, i, item);
                return;
            }
            String s = NPCShopItem.setMythicItem(item);
            if (s == null) return;
            pc.set(key, PersistentDataType.STRING, s);
        }
    }
}
