package net.azisaba.plugin.listeners;

import net.azisaba.plugin.ItemLoreEditEvent;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.npcshop.NPCShopItem;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ItemListener implements Listener {

    public void initialize(NPCShop shop) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new ItemListener.Lore(), shop);
    }

    public static class Lore extends ItemListener {

        @EventHandler
        public void onLore(@NotNull ItemLoreEditEvent e) {
            ItemStack item = e.getItem();
            List<String> list = new ArrayList<>(getTradeRequire(item));
            e.addLore(list);
        }
    }

    @NotNull
    public static List<String> getTradeRequire(@NotNull ItemStack item) {
        List<String> lore = new ArrayList<>();
        NPCShopItem.Deserializer data = new NPCShopItem.Deserializer(item, new ArrayList<>());
        if (data.list().isEmpty()) return lore;
        lore.add("");
        lore.add("§f§l- 要求交換素材 -");

        for (ItemStack is : data.list()) {
            if (is == null) continue;
            String display = LegacyComponentSerializer.legacyAmpersand().serialize(is.displayName()).replace("&", "§");
            lore.add("§f" + is.getAmount() + " × §r" + display);
        }
        lore.add("§f§l------------");
        return lore;
    }
}
