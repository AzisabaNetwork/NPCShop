package net.azisaba.plugin.listeners;

import com.github.bea4dev.artgui.menu.ArtGUIHolder;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.npcshop.NPCShopItem;
import net.azisaba.plugin.npcshop.ShopHolder;
import net.azisaba.plugin.npcshop.ShopUtil;
import net.azisaba.plugin.utils.Keys;
import net.azisaba.plugin.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.PluginManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class InventoryListener implements Listener {

    public void initialize(NPCShop plugin) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new InventoryListener.Click(), plugin);
        pm.registerEvents(new InventoryListener.Drag(), plugin);
        pm.registerEvents(new InventoryListener.Open(), plugin);
        pm.registerEvents(new InventoryListener.Close(), plugin);
    }

    protected void processInventory(ItemStack item, Inventory inv, int i, NamespacedKey key) {
        if (item == null) return;
        if (!Util.isMythicItem(item)) {
            ItemMeta meta = item.getItemMeta();
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "true");
            meta.getPersistentDataContainer().set(Keys.SHOP_MYTHIC, PersistentDataType.STRING,  item.getType().toString().toLowerCase());
            item.setItemMeta(meta);
            inv.setItem(i, item);
        }
    }

    public static class Open extends InventoryListener {

        @EventHandler
        public void onOpen(@NotNull InventoryOpenEvent e) {
            if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;
            Inventory inv = e.getPlayer().getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                processInventory(inv.getItem(i), inv, i, Keys.SHOP_EDITOR_TEMP);
            }
        }

        @EventHandler
        public void onBuy(@NotNull InventoryOpenEvent e) {
            if (!(e.getInventory().getHolder() instanceof ArtGUIHolder)) return;
            Inventory inv = e.getPlayer().getInventory();
            for (int i = 0; i < inv.getSize(); i++) {
                processInventory(inv.getItem(i), inv, i, Keys.SHOP_BUY_TEMP);
            }
        }
    }

    public static class Close extends InventoryListener {

        @EventHandler
        public void onClose(@NotNull InventoryCloseEvent e) {
            if (!(e.getInventory().getHolder() instanceof ShopHolder)) return;
            HumanEntity h = e.getPlayer();
            processInventory(h.getInventory(), Keys.SHOP_EDITOR_TEMP);
            Inventory inv = processInventory(e.getInventory(), Keys.SHOP_EDITOR_TEMP);
            for (ItemStack item : inv.getContents()) {
                if (item == null) continue;
                if (Util.isMythicItem(item)) {
                    h.getInventory().addItem(item).forEach((i, stack) -> h.getWorld().dropItem(h.getLocation(), stack));
                }
            }
        }

        @EventHandler
        public void onBuy(@NotNull InventoryCloseEvent e) {
            if (!(e.getInventory().getHolder() instanceof ArtGUIHolder)) return;
            processInventory(e.getPlayer().getInventory(), Keys.SHOP_BUY_TEMP);
        }

        public static Inventory processInventory(@NotNull Inventory inv, NamespacedKey key) {
            for (int i = 0; i < inv.getSize(); i++) {
                ItemStack item = inv.getItem(i);
                if (item == null) continue;
                ItemMeta meta = item.getItemMeta();
                if (meta.getPersistentDataContainer().has(Keys.SHOP_ITEM_DATA)) continue;
                if (meta.getPersistentDataContainer().has(key)) {
                    meta.getPersistentDataContainer().remove(key);

                    if (meta.getPersistentDataContainer().has(Keys.SHOP_MYTHIC)) {
                        meta.getPersistentDataContainer().remove(Keys.SHOP_MYTHIC);
                    }
                }
                item.setItemMeta(meta);
                inv.setItem(i, item);
            }
            return inv;
        }
    }

    public static class Click extends InventoryListener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onArtClick(@NotNull InventoryClickEvent e) {
            if (e.getInventory().getHolder() instanceof ArtGUIHolder) {
                Player p = (Player) e.getWhoClicked();
                if (p.getGameMode() == GameMode.CREATIVE) return;
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGH)
        public void onClick(@NotNull InventoryClickEvent e) {
            Inventory cnv = e.getClickedInventory();
            if (cnv == null) return;
            Inventory inv = e.getInventory();
            if (!(inv.getHolder() instanceof ShopHolder)) return;
            if (!(cnv.getHolder() instanceof ShopHolder)) {

                if (!e.getClick().isShiftClick()) return;
                e.setCancelled(true);
            } else {
                e.setCancelled(true);
                if (e.getSlot() == 1) return;
                if (e.getSlot() == 7) {
                    ItemStack check = inv.getItem(7);
                    if (check != null && check.getType().toString().toLowerCase().contains("green")) {
                        create(inv);
                        return;
                    }
                }
                if (e.getSlot() == 0) {
                    processItem(e, e.getCursor(), Util.isMythicItem(e.getCursor()));
                } else if (e.getSlot() < 7) {
                    processItem(e, e.getCursor(), Util.isMythicItem(e.getCursor()) && !ShopUtil.isShopItemsData(e.getCursor()));

                } else if (e.getSlot() == 8) {
                    ItemStack check = inv.getItem(8);
                    if (check!= null && Util.isMythicItem(check) && check.getType() != ShopHolder.getPane().getType()) {
                        e.setCurrentItem(ShopHolder.getPane());
                        e.setCursor(check);
                    }
                }
            }
        }

        public void processItem(InventoryClickEvent e, ItemStack cur, boolean is) {
            if (cur == null) {
                if (e.getCurrentItem() != null) {
                    e.setCursor(null);
                    e.setCurrentItem(ShopHolder.getPane());
                } else {
                    e.setCurrentItem(ShopHolder.getPane());
                }
            } else {
                if (!(is)) return;
                if (e.getCurrentItem() != null) {
                    e.setCursor(null);
                    e.setCurrentItem(cur);
                } else {
                    e.setCurrentItem(cur);
                }
            }
            update(e.getInventory(), e.getSlot());
        }

        public void update(@NotNull Inventory inventory, int slot) {
            ItemStack curr = inventory.getItem(slot);
            if (curr == null) {
                inventory.setItem(slot, ShopHolder.getPane());
            } else {
               ItemStack i1 = inventory.getItem(0);
               int c1 = 0;
               int c2 = 0;
               if (i1 == null || !Util.isMythicItem(i1)) {
                   inventory.setItem(0, ShopHolder.getPane());
               } else {
                   c1++;
               }

               for (int i = 2; i < 7; i++) {
                   ItemStack i2 = inventory.getItem(i);
                   if (i2 == null || !Util.isMythicItem(i2) || ShopUtil.isShopItemsData(i2)) {
                       inventory.setItem(i, ShopHolder.getPane());
                   } else {
                       c2++;
                       break;
                   }
               }

               if (c1 == 1 && c2 == 1) {
                   inventory.setItem(1, ShopHolder.getGreenSet());
                   inventory.setItem(7, ShopHolder.getGreenCreate());
               } else if ((c1 == 1 && c2 == 0) || c1 == 0 && c2 == 1) {
                   inventory.setItem(1, ShopHolder.getYellowSet());
                   inventory.setItem(7, ShopHolder.getYellowCreate());
               } else {
                   inventory.setItem(1, ShopHolder.getRedSet());
                   inventory.setItem(7, ShopHolder.getRedCreate());
               }
            }
        }

        public void create(@NotNull Inventory inv) {
            ItemStack s0 = inv.getItem(0);
            if (s0 == null) return;

            List<ItemStack> list = new ArrayList<>();
            for (int i = 2; i < 7; i++) {

                ItemStack ss = inv.getItem(i);
                if (ss == null || !Util.isMythicItem(ss)) continue;
                list.add(ss);
            }
            ItemStack item = new NPCShopItem.Serializer(s0, list).item();
            for (int i = 0; i < inv.getSize(); i++) {
                inv.setItem(i, ShopHolder.getPane());
            }
            inv.setItem(1, ShopHolder.getRedSet());
            inv.setItem(7, ShopHolder.getRedCreate());
            inv.setItem(8, item);
        }
    }

    public static class Drag extends InventoryListener {

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void onArtDrag(@NotNull InventoryDragEvent e) {
            if (e.getInventory().getHolder() instanceof ArtGUIHolder) {
                Player p = (Player) e.getWhoClicked();
                if (p.getGameMode() == GameMode.CREATIVE) return;
                e.setCancelled(true);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
        public void ondrag(@NotNull InventoryDragEvent e) {
            if (e.getInventory().getHolder() instanceof ShopHolder) {
                e.setCancelled(true);
            }
        }
    }
}
