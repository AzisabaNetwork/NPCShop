package net.azisaba.plugin.commands;

import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.database.DBShop;
import net.azisaba.plugin.database.SaveDB;
import net.azisaba.plugin.utils.holder.ShopHolder;
import net.azisaba.plugin.utils.shop.ShopLocation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
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

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (strings.length == 1) {
            if (strings[0].equalsIgnoreCase("editor")) editor(player);
            if (strings[0].equalsIgnoreCase("remove")) remove(player);
            if (strings[0].equalsIgnoreCase("sync")) sync();
            if (strings[0].equalsIgnoreCase("reload")) reload();
            if (strings[0].equalsIgnoreCase("reset")) reset();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (!(commandSender instanceof Player)) return null;
        if (strings.length != 1) return null;

        String get = strings[0];
        List<String> list = new ArrayList<>();
        Set<String> set = new HashSet<>(Set.of("remove", "editor", "sync", "reload", "reset"));
        for (String ss : set) {
            if (ss.contains(get.toLowerCase())) {
                list.add(ss);
            }
        }
        return list;
    }

    private void editor(@NotNull Player player) {
        player.closeInventory();
        player.openInventory(new ShopHolder().getInventory());
    }

    private void remove(@NotNull Player player) {
        RayTraceResult result = player.rayTraceBlocks(4.5, FluidCollisionMode.NEVER);
        if (result == null || result.getHitBlock() == null || result.getHitBlockFace() == null) {
            player.sendMessage(Component.text("視線の先にブロックがありません。", NamedTextColor.RED));
            return;
        }
        Location loc = result.getHitBlock().getLocation();
        Block b = loc.getBlock().getRelative(result.getHitBlockFace());
        ShopLocation newLocation = new ShopLocation(b.getWorld(), b.getX() , b.getY(), b.getZ());

        if (!DBShop.getShopItems().containsKey(newLocation)) {
            player.sendMessage(Component.text("ショップがありません。", NamedTextColor.RED));
            return;
        }

        DBShop.removeShopItems(newLocation);
        player.sendMessage(Component.text("ショップを削除しました。", NamedTextColor.RED));
    }

    private void sync() {
        DBShop.getShopItems().clear();
        JavaPlugin.getPlugin(NPCShop.class).runAsync(()-> new DBShop().load());
    }

    private void reload() {
        JavaPlugin.getPlugin(NPCShop.class).runAsync(()-> new SaveDB().saveShops(false, false));
    }

    private void reset() {
        JavaPlugin.getPlugin(NPCShop.class).runAsync(()-> new SaveDB().saveShops(false, true));
    }
}
