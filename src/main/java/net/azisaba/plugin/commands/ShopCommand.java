package net.azisaba.plugin.commands;

import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.data.SaveDB;
import net.azisaba.plugin.npcshop.NPCEntity;
import net.azisaba.plugin.npcshop.ShopHolder;
import net.azisaba.plugin.npcshop.ShopLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            if (strings[0].equalsIgnoreCase("overwriteSave")) return overwriteSave();
            if (strings[0].equalsIgnoreCase("removeSave")) return removeSave(player);
            if (strings[0].equalsIgnoreCase("reload")) return reload(player);
        } else if (strings.length == 2) {
            if (strings[0].equalsIgnoreCase("create")) {

                ShopLocation shop = getTargetBlock(player);
                if (shop == null) {
                    player.sendMessage(Component.text("視線4.5ブロック以内に固体ブロックがありません。", NamedTextColor.RED));
                    return true;
                }

                EntityType t = EntityType.VILLAGER;
                try {
                    t = EntityType.fromName(strings[1]);
                } catch (Exception ignored) {
                }

                EntityType finalT = t;
                getPlugin().runSync(()-> new NPCEntity().spawn(ShopLocation.adapt(shop), finalT));
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
            Set<String> set = new HashSet<>(Set.of("create", "itemEditor", "addSave", "overwriteSave", "removeSave", "reload"));
            for (String ss : set) {
                if (ss.toLowerCase().contains(get.toLowerCase())) {
                    list.add(ss);
                }
            }
            return list;
        } else if (strings.length == 2 && strings[0].equalsIgnoreCase("create")) {
            List<String> list = new ArrayList<>();
            for (EntityType t : EntityType.values()) {
                list.add(t.name());
            }
            return list;
        }
        return null;
    }

    private boolean itemEditor(@NotNull Player player) {
        player.closeInventory();
        player.openInventory(new ShopHolder().getInventory());
        return true;
    }

    private boolean reload(@NotNull Player p) {
        getPlugin().saveDefaultConfig();
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

        DBShop.removeShopItems(newLocation);
        overwriteSave();
        player.sendMessage(Component.text("ショップを削除しました。", NamedTextColor.RED));
        return true;
    }

    private void sync() {
        DBShop.getShopItems().clear();
        if (!getPlugin().getConfig().getBoolean("Database.use", false)) return;
        getPlugin().runAsync(()-> new DBShop().load());
    }

    private boolean addSave() {
        getPlugin().runAsync(()-> new SaveDB().saveShops(false, false));
        sync();
        return true;
    }

    private boolean overwriteSave() {
        getPlugin().runAsync(()-> new SaveDB().saveShops(false, true));
        sync();
        return true;
    }
}
