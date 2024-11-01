package net.azisaba.plugin.lore;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class LoreHandler {

    @NotNull
    @Contract("_, _ -> param1")
    public static ItemStack setTagInLore(@NotNull ItemStack item, List<String> set) {
        ItemMeta meta = item.getItemMeta();
        if (meta.hasLore()) {
            List<Component> list = meta.lore();
            List<Component> comp = list != null ? new ArrayList<>(list) : new ArrayList<>();
            meta.lore(put(comp, set));
        } else {
            List<Component> comp = put(new ArrayList<>(), set);
            meta.lore(comp);
        }
        item.setItemMeta(meta);
        return item;
    }

    @Contract("_, _ -> param1")
    private static List<Component> put(List<Component> comp, @NotNull List<String> set) {
        for (String s : set) {
            comp.add(LegacyComponentSerializer.legacyAmpersand().deserialize(s));
        }
        return comp;
    }

    @Contract("_, _ -> param1")
    public static @NotNull ItemStack removeTagInLore(@NotNull ItemStack item, ItemStack serverItem) {
        ItemMeta meta = item.getItemMeta();
        if (serverItem == null || !serverItem.hasItemMeta() || !serverItem.getItemMeta().hasLore()) {
            meta.lore(null);
        } else {
            meta.lore(serverItem.lore());
        }
        item.setItemMeta(meta);
        return item;
    }
}
