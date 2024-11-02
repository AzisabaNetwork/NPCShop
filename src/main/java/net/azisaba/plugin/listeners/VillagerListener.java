package net.azisaba.plugin.listeners;

import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.npcshop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerCareerChangeEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class VillagerListener implements Listener {

    public void initialize(NPCShop npcShop) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new VillagerListener.Career(), npcShop);
    }

    public static class Career extends VillagerListener {

        @EventHandler
        public void onVillagerCareer(@NotNull VillagerCareerChangeEvent e) {
            Villager v = e.getEntity();
            if (ShopUtil.isShop(v)) {
                e.setCancelled(true);
            }
        }
    }
}
