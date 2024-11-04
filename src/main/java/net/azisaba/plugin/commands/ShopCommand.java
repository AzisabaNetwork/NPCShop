package net.azisaba.plugin.commands;

import me.libraryaddict.disguise.DisguiseAPI;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.data.SaveDB;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.npcshop.NPCEntity;
import net.azisaba.plugin.npcshop.ShopEntity;
import net.azisaba.plugin.npcshop.ShopHolder;
import net.azisaba.plugin.npcshop.ShopLocation;
import net.azisaba.plugin.utils.Keys;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ShopCommand implements TabExecutor {

    @NotNull
    private NPCShop getPlugin() {
        return JavaPlugin.getPlugin(NPCShop.class);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length == 1) {
            if (strings[0].equalsIgnoreCase("itemEditor")) return itemEditor(player);
            if (strings[0].equalsIgnoreCase("addSave")) return addSave();
            if (strings[0].equalsIgnoreCase("removeSave")) return removeSave(player);
            if (strings[0].equalsIgnoreCase("reload")) return reload(player);
            if (strings[0].equalsIgnoreCase("clearSave")) return clearSave(player);
            if (strings[0].equalsIgnoreCase("show")) return show(player, 1);
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("create")) {

                ShopLocation shop = getTargetBlock(player);
                if (shop == null) {
                    player.sendMessage(Component.text("視線4.5ブロック以内に固体ブロックがありません。", NamedTextColor.RED));
                    return true;
                }

                EntityType t = EntityType.VILLAGER;
                try {
                    EntityType tt = EntityType.fromName(strings[1]);
                    if (tt != null && tt.isAlive() && tt.isSpawnable()) {
                        t = tt;
                    }
                } catch (Exception ignored) {
                }

                EntityType finalT = t;
                getPlugin().runSync(()-> new NPCEntity().spawn(ShopLocation.adapt(shop), finalT));
            } else if (strings[0].equalsIgnoreCase("kill")) {

                int range;
                try {
                    range = Integer.parseInt(strings[1]);
                } catch (NumberFormatException e) {
                    range = 1;
                }
                return kill(player, range);

            } else if (strings[0].equalsIgnoreCase("show")) {
                int page;
                try {
                    page = Integer.parseInt(strings[1]);
                } catch (NumberFormatException e) {
                    page = 1;
                }
                show(player, page);
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length == 1) {

            String get = strings[0];
            List<String> list = new ArrayList<>();
            Set<String> set = new HashSet<>(Set.of("create", "itemEditor", "addSave", "removeSave", "reload", "kill", "show"));
            for (String ss : set) {
                if (ss.toLowerCase().contains(get.toLowerCase())) {
                    list.add(ss);
                }
            }
            return list;
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("create")) {
                List<String> list = new ArrayList<>();
                for (EntityType t : EntityType.values()) {
                    if (t.isAlive() && t.isSpawnable() && t.name().contains(strings[1].toUpperCase())) {
                        list.add(t.name());
                    }
                }
                return list;
            } else if (strings[0].equalsIgnoreCase("kill")) {
               return List.of("1", "2", "3", "4", "5");
            }
        }
        return null;
    }

    private boolean kill(@NotNull Player p, int range) {
        int count = 0;
        for (LivingEntity living : p.getLocation().getNearbyLivingEntities(range).stream().toList()) {
            if (living.getPersistentDataContainer().has(Keys.SHOP_KEEPER)) {

                if (DisguiseAPI.isDisguised(living)) {
                    DisguiseAPI.getDisguise(living).stopDisguise();
                }
                living.remove();
                count++;
            }
        }
        p.sendMessage(Component.text(count + "体のShopEntityを削除しました。"));
        return true;
    }

    private boolean itemEditor(@NotNull Player player) {
        player.closeInventory();
        player.openInventory(new ShopHolder().getInventory());
        return true;
    }

    private boolean reload(@NotNull Player p) {
        getPlugin().reloadConfig();
        p.sendMessage(Component.text("リロードしました。"));
        return true;
    }

    @Nullable
    private ShopLocation getTargetBlock(@NotNull Player p) {
        RayTraceResult result = p.rayTraceBlocks(4.5, FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null || result.getHitBlockFace() == null) {
            return null;
        }
        Location loc = result.getHitBlock().getLocation();
        Block b = loc.getBlock().getRelative(result.getHitBlockFace());
        return new ShopLocation(b.getWorld(), b.getX() , b.getY(), b.getZ());
    }

    private boolean removeSave(@NotNull Player player) {
        ShopLocation newLocation = getTargetBlock(player);
        if (newLocation == null) {
            player.sendMessage(Component.text("視線4.5ブロック以内に固体ブロックがありません。", NamedTextColor.RED));
            return false;
        }

        if (!DBShop.getShopItems().containsKey(newLocation)) {
            player.sendMessage(Component.text("ショップがありません。", NamedTextColor.RED));
            return true;
        }

        DBShop.removeShop(newLocation);
        overwriteSave();
        NPCEntity.despawn(newLocation);
        player.sendMessage(Component.text("ショップを削除しました。", NamedTextColor.RED));
        return true;
    }

    private void sync() {
        getPlugin().runAsyncDelayed(()-> {
            DBShop.clear();
            if (!getPlugin().getConfig().getBoolean("Database.use", false)) return;
            new DBShop().load();
        }, 10);
    }

    private boolean addSave() {
        getPlugin().runAsync(()-> new SaveDB().saveShops(false, false));
        sync();
        return true;
    }

    private void overwriteSave() {
        getPlugin().runAsync(()-> new SaveDB().saveShops(false, true));
        sync();
    }

    private boolean clearSave(@NotNull Player p) {
        if (!p.getName().equalsIgnoreCase("MCLove32")) return false;
        getPlugin().runAsync(()-> new SaveDB().deleteAll());
        sync();
        p.sendMessage(Component.text("データを全削除しました。"));
        return true;
    }

    private boolean show(@NotNull Player player, int page) {
        if (DBShop.getShopEntity().isEmpty()) {
            player.sendMessage(Component.text("データはありません。"));
            return false;
        }
        int skip = (page - 1) * 10;
        while (DBShop.getShopEntity().values().size() - skip < 0) {
            skip-= 10;
        }
        int base = skip / 10;
        int message1 = base+ 1;
        int message2 = base * 10 + 1;
        int repeat = 0;
        for (Map.Entry<ShopLocation, ShopEntity> entry : DBShop.getShopEntity().entrySet()) {
            if (skip > 0) {
                skip--;
                continue;
            }
            if (repeat >= 10) {
                break;
            }
            ShopLocation loc = entry.getKey();
            ShopEntity shop = entry.getValue();
            player.sendMessage(Component.text("ショップ名: " + shop.name() + " ショップタイプ: " + shop.type() + " 場所: " + loc.w().getName() + ", " + loc.x() + ", " + loc.y() + ", " + loc.z()));
            repeat++;
        }
        int message3 = base * 10 + repeat;
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize("&b" + message1 + "&fページ目 &6[" + message2 + "~" + message3 + "] &5全データ&e" + DBShop.getShopEntity().size() + "&5件"));
        return true;
    }
}
