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
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.npcshop.*;
import net.azisaba.plugin.utils.CoolTime;
import net.azisaba.plugin.utils.Keys;
import net.azisaba.plugin.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
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
        pm.registerEvents(new PlayerListener.Join(), npcShop);
    }

    public static class Join extends PlayerListener {

        @EventHandler
        public void onJoin(@NotNull PlayerJoinEvent e) {
            JavaPlugin.getPlugin(NPCShop.class).runAsyncDelayed(()-> {
                InventoryListener.Close.processInventory(e.getPlayer().getInventory(), Keys.SHOP_BUY_TEMP);
                InventoryListener.Close.processInventory(e.getPlayer().getInventory(), Keys.SHOP_EDITOR_TEMP);
            }, 10);
        }
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
                ItemStack main = p.getInventory().getItemInMainHand();
                clicked.setRotation(p.getYaw() + 180, 0);
                e.setCancelled(true);

                if (p.getGameMode() == GameMode.CREATIVE) {
                    String tag = null;
                    if (main.getType() == Material.NAME_TAG) {
                        String s = ShopEntity.getString(main.displayName());
                        if (s != null && !s.isEmpty() && !s.isBlank()) {
                            tag = s;
                        }
                    }
                    String st = clicked.getPersistentDataContainer().get(Keys.SHOP_TYPE, PersistentDataType.STRING);
                    EntityType type = st == null ? null : EntityType.fromName(st);
                    DBShop.setShopEntity(ShopLocation.adapt(clicked.getLocation()), type, tag);
                    if (tag != null) {
                        p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(tag + " &fに名前を変更しました。"));
                    }
                }

                if (p.isSneaking()) {
                    if (p.getGameMode() == GameMode.CREATIVE) {

                        if (ShopUtil.isShopItemsData(main)) {
                            DBShop.setShopItems(ShopLocation.adapt(clicked.getLocation()), main);
                            p.sendMessage(Component.text("トレードを追加しました。", NamedTextColor.GREEN));
                        }
                    }
                } else {
                    openShopScreen(p, clicked.getLocation());
                }
            }

            private void openShopScreen(Player p, @NotNull Location location) {
                ShopLocation loc = new ShopLocation(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
                List<ItemStack> list = getShopContents(loc);
                Artist artist = artGui();
                ArtMenu artMenu = artist.createMenu(JavaPlugin.getPlugin(NPCShop.class).getArtGUI(), "&b&lNPCShop &r[{CurrentPage}/{MaxPage}]");
                artMenu.asyncCreate(menu -> {

                    int count = 0;
                    if (!list.isEmpty()) {
                        for (ItemStack stack : list) {
                            menu.setButton(getPage(count), getSlot(count), new ArtButton(stack).listener((e, menu1) -> onArtScreenEvent(e, location)));
                            count++;
                        }
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

            private void onArtScreenEvent(@NotNull InventoryClickEvent e, Location location) {
                if (!(e.getInventory().getHolder() instanceof ArtGUIHolder)) return;
                Player p = (Player) e.getWhoClicked();
                if (p.getGameMode() == GameMode.CREATIVE && creativeAction(e, location)) return;

                e.setCancelled(true);
                ItemStack current = e.getCurrentItem();
                if (!ShopUtil.isShopItemsData(current)) return;

                if (CoolTime.isCoolTime(getClass(), p, ct)) return; //CT
                CoolTime.setCoolTime(getClass(), p, ct, 4);

                int multiplier = 1;
                if (e.isShiftClick()) multiplier = 16;

                NPCShopItem.Deserializer outPut = new NPCShopItem.Deserializer(current, new ArrayList<>()); //処理本体
                String mmid = Util.getMythicID(current);
                if (mmid == null) return;

                List<String> idList = new ArrayList<>();
                List<Integer> countList = new ArrayList<>();
                for (ItemStack stack : outPut.list()) {
                    String s = Util.getMythicID(stack) == null ? stack.getType().toString().toLowerCase() : Util.getMythicID(stack);
                    idList.add(s);
                    countList.add(stack.getAmount() * multiplier);
                }

                if (!idList.isEmpty() && !countList.isEmpty() && has(p, idList, countList) && !p.getInventory().isEmpty()) {
                    buy(p, idList, countList, mmid, current.getAmount() * multiplier);
                    return;
                }

                p.sendMessage(Component.text("素材が足りません。", NamedTextColor.RED));
                p.playSound(p, Sound.ENTITY_VILLAGER_NO, 1, 1);
            }

            private boolean creativeAction(@NotNull InventoryClickEvent e, @NotNull Location loc) {
                ItemStack current = e.getCurrentItem();
                ItemStack cursor = e.getCursor();
                ShopLocation shop = ShopLocation.adapt(loc);

                List<ItemStack> list = new ArrayList<>(DBShop.getShopItems().get(shop));
                int i = list.indexOf(current);
                if (i == -1) return false;
                if (current != null) {
                    list.add(i, cursor);
                    i++;
                }
                list.remove(i);

                e.setCurrentItem(cursor);
                e.setCursor(current);

                DBShop.replaceShopItem(shop, list);
                return true;
            }

            private void give(Player p, String mmid, int getAmount) {
                ItemStack item = Util.getMythicItemStack(mmid, getAmount);
                if (item == null) return;
                for (ItemStack stack : p.getInventory().addItem(item).values()) {
                    p.getWorld().dropItemNaturally(p.getLocation(), stack);
                }
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

            private void bought(@NotNull Player p, String mmid, int getAmount) {
                p.sendMessage(Component.text("商品を購入しました。", NamedTextColor.GREEN));
                p.playSound(p, Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                p.playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                p.playSound(p, Sound.ENTITY_ARROW_HIT_PLAYER, 1, 2);
                give(p, mmid, getAmount);
                p.playSound(p, Sound.ENTITY_VILLAGER_YES, 1, 1);
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

            private void processItems(@NotNull Player p, @NotNull List<String> stringList, @NotNull List<Integer> countList, BiFunction<ItemStack, Integer, Integer> processor) {
                for (ItemStack item : p.getInventory().getContents()) {
                    if (item == null) continue;
                    String id = Util.getMythicID(item);
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

            @NotNull
            private List<ItemStack> getShopContents(ShopLocation loc) {
                List<ItemStack> list = new ArrayList<>();
                if (DBShop.itemContains(loc)) {
                    list.addAll(DBShop.getShopItems().get(loc));
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
                            if (!ShopUtil.isShopItemsData(item)) continue;
                            list.add(item);
                        }
                        continue;
                    }
                    Map<Integer, Object> map = menu.getPageComponents(i);
                    if (map == null) continue;
                    for (Object obj : map.values().stream().toList()) {
                        if (obj instanceof ArtButton button) {
                            ItemStack stack = button.getItemStack();
                            if (!ShopUtil.isShopItemsData(stack)) continue;
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
