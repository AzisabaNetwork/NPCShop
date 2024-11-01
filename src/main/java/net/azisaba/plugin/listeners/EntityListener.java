package net.azisaba.plugin.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import me.libraryaddict.disguise.DisguiseAPI;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.npcshop.ShopUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

public class EntityListener implements Listener {
    public void initialize(NPCShop npcShop) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new EntityListener.Damage(), npcShop);
        pm.registerEvents(new EntityListener.Death(), npcShop);
        pm.registerEvents(new EntityListener.Remove(), npcShop);
    }

    public static class Damage extends EntityListener {

        @EventHandler
        public void onDamage(@NotNull EntityDamageEvent e) {
            if (e.getEntity() instanceof Villager v && ShopUtil.isShop(v)) {
                e.setCancelled(true);
            }
        }
    }

    public static class Death extends EntityListener {

        @EventHandler
        public void onDamage(@NotNull EntityDeathEvent e) {
            if (e.getEntity() instanceof Villager v && ShopUtil.isShop(v)) {
                e.setCancelled(true);
                v.setHealth(v.getMaxHealth());
            }
        }
    }

    public static class Remove extends EntityListener {

        @EventHandler
        public void onDamage(@NotNull EntityRemoveFromWorldEvent e) {
            if (e.getEntity() instanceof Villager v && ShopUtil.isShop(v) && DisguiseAPI.isDisguised(v)) {
                DisguiseAPI.getDisguise(v).stopDisguise();
            }
        }
    }
}
