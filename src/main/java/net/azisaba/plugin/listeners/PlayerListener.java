package net.azisaba.plugin.listeners;

import com.github.bea4dev.artgui.button.ArtButton;
import com.github.bea4dev.artgui.button.PageBackButton;
import com.github.bea4dev.artgui.button.PageNextButton;
import com.github.bea4dev.artgui.button.ReplaceableButton;
import com.github.bea4dev.artgui.frame.Artist;
import com.github.bea4dev.artgui.menu.ArtGUIHolder;
import com.github.bea4dev.artgui.menu.ArtMenu;
import com.github.bea4dev.artgui.menu.Menu;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import io.lumine.mythic.bukkit.MythicBukkit;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.database.DBShop;
import net.azisaba.plugin.utils.*;
import net.azisaba.plugin.utils.shop.NPCShopItem;
import net.azisaba.plugin.utils.shop.ShopItemBuilder;
import net.azisaba.plugin.utils.shop.ShopLocation;
import net.azisaba.plugin.utils.shop.ShopUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;

public class PlayerListener implements Listener {

    public void initialize(NPCShop npcShop) {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerListener.Interact.Entity(), npcShop);
    }

    public static class Interact extends PlayerListener {

        public static class Entity extends Interact {

            private static final Multimap<Class<?>, UUID> ct = HashMultimap.create();

            @EventHandler
            public void onInteractEntity(@NotNull PlayerInteractEntityEvent e) {
                if (e.getHand() != EquipmentSlot.HAND) return;
                Player p = e.getPlayer();
                org.bukkit.entity.Entity clicked = e.getRightClicked();

                if (!ShopUtil.isShop(clicked)) return;
                if (p.isSneaking()) {
                    if (p.getGameMode() == GameMode.CREATIVE) {

                        e.setCancelled(true);
                        ItemStack main = p.getInventory().getItemInMainHand();
                        Location loc = clicked.getLocation();

                        if (ShopUtil.isShopItemsData(main)) {
                            DBShop.setShopItems(ShopLocation.adapt(loc), main);
                            p.sendMessage(Component.text("トレードを追加しました。", NamedTextColor.GREEN));
                        }
                    }
                } else {
                    openShopScreen(p, clicked.getLocation());
                }
            }

            private void openShopScreen(Player p, @NotNull Location location) {
                ShopLocation loc = new ShopLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
                List<ItemStack> list = getShopContents(location);
                Artist artist = artGui();
                ArtMenu artMenu = artist.createMenu(JavaPlugin.getPlugin(NPCShop.class).getArtGUI(), "&b&lNPCShop &r[{CurrentPage}/{MaxPage}]");
                artMenu.asyncCreate(menu -> {

                    int count = 0;
                    for (ItemStack stack : list) {
                        menu.setButton(getPage(count), getSlot(count), new ArtButton(stack).listener((e, menu1) -> onArtScreenEvent(e, location)));
                    }
                });
                artMenu.onClose((event, menu) -> {
                    Player player = (Player) event.getPlayer();
                    if (player.getGameMode() != GameMode.CREATIVE) return;
                    List<ItemStack> get = new ArrayList<>(
                            getItemStacks(menu, getCurrentPage(menu.getNamedInventory().replacedName), event.getInventory()));
                    DBShop.replaceShopItem(loc, get);
                });
                artMenu.open(p);
            }

            private void onArtScreenEvent(@NotNull InventoryClickEvent e, Location loc) {
                if (!(e.getInventory().getHolder() instanceof ArtGUIHolder)) return;
                Player p = (Player) e.getWhoClicked();
                if (p.getGameMode() == GameMode.CREATIVE && creativeAction(e, loc)) return;

                e.setCancelled(true);
                ItemStack current = e.getCurrentItem();
                if (!ShopUtil.isShopItemsData(current)) return;

                if (CoolTime.isCoolTime(getClass(), p, ct)) return; //CT
                CoolTime.setCoolTime(getClass(), p, ct, 4);

                int multiplier = 1;
                if (e.isShiftClick()) multiplier = 16;

                NPCShopItem.Deserializer outPut = new NPCShopItem.Deserializer(current, new ArrayList<>()); //処理本体
                ItemStack origin = outPut.item();
                String mmid = MythicUtil.getMythicID(origin);
                if (mmid == null) return;

                List<String> idList = new ArrayList<>();
                List<Integer> countList = new ArrayList<>();
                for (ItemStack stack : outPut.list()) {
                    String s = MythicUtil.getMythicID(stack);
                    if (s == null) continue;
                    idList.add(s);
                    countList.add(stack.getAmount() * multiplier);
                }

                if (has(p, idList, countList)) {
                    buy(p, idList, countList, mmid, origin.getAmount() * multiplier);
                    return;
                }

                p.sendMessage(Component.text("素材が足りません。", NamedTextColor.RED));
                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
            }

            private boolean creativeAction(@NotNull InventoryClickEvent e, Location loc) {
                ItemStack current = e.getCurrentItem();
                ItemStack cursor = e.getCursor();

                List<ItemStack> list = new ArrayList<>(DBShop.getShopItems().get(ShopLocation.adapt(loc)));
                int i = list.indexOf(current);
                if (current != null) {
                    list.add(i, cursor);
                    i++;
                }
                list.remove(i);

                e.setCurrentItem(cursor);
                e.setCursor(current);

                DBShop.replaceShopItem(ShopLocation.adapt(loc), list);
                return true;
            }

            private void buy(@NotNull Player p, @NotNull List<String> stringList, List<Integer> countList, String mmid, int getAmount) {
                processItems(p, stringList, countList, (item, amount) -> {
                    if (item.getAmount() < amount) {
                        amount -= item.getAmount();
                        item.setAmount(0);
                        return amount;
                    } else {
                        item.setAmount(item.getAmount() - amount);
                        return 0;
                    }
                });
                bought(p, mmid, getAmount);
            }

            private void processItems(@NotNull Player p, @NotNull List<String> stringList, @NotNull List<Integer> countList, BiFunction<ItemStack, Integer, Integer> processor) {
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item == null) continue;
                    String id = MythicUtil.getMythicID(item);
                    if (id == null || !stringList.contains(id)) continue;

                    int size = getIndex(stringList, id);
                    int amount = countList.get(size);
                    amount = processor.apply(item, amount);
                    if (amount == 0) {
                        countList.remove(size);
                        stringList.remove(size);
                    } else {
                        countList.set(size, amount);
                    }
                }
            }

            private void bought(@NotNull Player p, String mmid, int getAmount) {
                p.sendMessage(Component.text("商品を購入しました。", NamedTextColor.GREEN));
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 2);
                give(p, mmid, getAmount);
                p.playSound(p, Sound.ENTITY_VILLAGER_YES, 1, 1);
            }

            private void give(Player p, String mmid, int getAmount) {
                ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(mmid, getAmount);
                if (item == null) return;
                for (ItemStack stack : p.getInventory().addItem(item).values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), stack);
                }
            }

            private boolean has(@NotNull Player p, @NotNull List<String> stringList, List<Integer> countList) {
                List<String> stringListCopy = new ArrayList<>(stringList);
                List<Integer> countListCopy = new ArrayList<>(countList);

                processItems(p, stringListCopy, countListCopy, (item, amount) -> {
                    if (item.getAmount() < amount) {
                        amount -= item.getAmount();
                        return amount;
                    } else {
                        return 0;
                    }
                });
                return stringListCopy.isEmpty() && countListCopy.isEmpty();
            }

            @NotNull
            private List<ItemStack> getShopContents(Location loc) {
                List<ItemStack> list = new ArrayList<>();
                if (DBShop.contains(loc)) {
                    list.addAll(DBShop.getShopItems().get(ShopLocation.adapt(loc)));
                }
                return list;
            }


            public Artist artGui() {
                return new Artist(()-> {

                    ArtButton V = null;
                    ArtButton B = new ArtButton(new ShopItemBuilder(Material.WHITE_STAINED_GLASS_PANE).name("&f").build());
                    PageNextButton N = new PageNextButton(new ShopItemBuilder(Material.ARROW).name("&r次のページ &7[{NextPage}/{MaxPage}]").build());
                    PageBackButton P = new PageBackButton(new ShopItemBuilder(Material.ARROW).name("&r前のページ &7[{PreviousPage}/{MaxPage}]").build());
                    ReplaceableButton I = new ReplaceableButton(new ShopItemBuilder(Material.NAME_TAG).name("&7現在のページ&r[{CurrentPage}/{MaxPage}]").build());

                    return new ArtButton[]{
                            B, B, B, B, B, B, B, B, B,
                            B, V, V, V, V, V, V, V, B,
                            B, V, V, V, V, V, V, V, B,
                            B, V, V, V, V, V, V, V, B,
                            B, V, V, V, V, V, V, V, B,
                            P, B, B, B, I, B, B, B, N
                    };
                });
            }

            public int getCurrentPage(@NotNull String title) {
                return Integer.parseInt(title.substring(title.indexOf("/") - 1, title.indexOf("/"))) - 1;
            }

            public int getIndex(@NotNull List<String> list, String id) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).equals(id)) return i;
                }
                return -1;
            }

            @NotNull
            public List<ItemStack> getItemStacks(@NotNull Menu menu, int count, Inventory inv) {
                List<ItemStack> list = new ArrayList<>();
                for (int i = 0; i <= menu.getCurrentMaxPage(); i++) {
                    if (i == count) {
                        for (ItemStack item : inv.getContents()) {
                            if (ShopUtil.isShopItem(item)) continue;
                            list.add(item);
                        }
                        continue;
                    }
                    Map<Integer, Object> map = menu.getPageComponents(i);
                    if (map == null) continue;
                    for (Object obj : map.values().stream().toList()) {
                        if (obj instanceof ArtButton button) {
                            ItemStack stack = button.getItemStack();
                            if (ShopUtil.isShopItem(stack)) continue;
                            list.add(stack);
                        }
                    }
                }
                return list;
            }

            public int getPage(int count) {
                return (count / 28);
            }

            public int getSlot(int count) {
                int get = Math.floorMod(count, 28);
                if (get < 7) {
                    return get + 10;
                } else if (get < 14) {
                    return get + 12;
                } else if (get < 21) {
                    return get + 14;
                } else return get + 16;
            }
        }
    }
}
