package net.azisaba.plugin.utils.shop;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ShopLocation(World w, int x, int y, int z) {

    public boolean isSimilar(@NotNull Location loc) {
        if (!loc.getWorld().equals(w())) return false;
        if (loc.getBlockX() != x()) return false;
        if (loc.getBlockY() != y()) return false;
        return loc.getBlockZ() == z();
    }

    @Contract("_ -> new")
    public static @NotNull ShopLocation adapt(@NotNull Location loc) {
        return new ShopLocation(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }
}
