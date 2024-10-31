package net.azisaba.plugin.npcshop;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ShopLocation(World w, int x, int y, int z) {

    @Contract("_ -> new")
    public static @NotNull ShopLocation adapt(@NotNull Location loc) {
        return new ShopLocation(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @NotNull
    @Contract(value = "_ -> new", pure = true)
    public static Location adapt(@NotNull ShopLocation loc) {
        return new Location(loc.w, loc.x + 0.5, loc.y, loc.z + 0.5);
    }
}
