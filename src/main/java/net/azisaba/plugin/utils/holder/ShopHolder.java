package net.azisaba.plugin.utils.holder;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class ShopHolder implements InventoryHolder {


    @Override
    public @NotNull Inventory getInventory() {
        Inventory inv = Bukkit.createInventory(this, 9, Component.text("ShopItemBuilder", NamedTextColor.AQUA));
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, getPane());
        }
        inv.setItem(1, getRedSet());
        inv.setItem(7, getRedCreate());
        return inv;
    }

    @NotNull
    @Contract(" -> new")
    public static ItemStack getPane() {
        return new ItemStack(Material.WHITE_STAINED_GLASS_PANE);
    }

    @NotNull
    public static ItemStack getRedSet() {
        ItemStack barrier = new ItemStack(Material.RED_TERRACOTTA);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&c左にベースアイテムをセット、右に要求する予定のアイテムをセット&c&l✖"));
        barrier.setItemMeta(meta);

        return barrier;
    }

    @NotNull
    public static ItemStack getRedCreate() {
        ItemStack barrier = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&cアイテムがセットされていないため生成できません。&c&l✖"));
        barrier.setItemMeta(meta);

        return barrier;
    }

    @NotNull
    public static ItemStack getYellowSet() {
        ItemStack barrier = new ItemStack(Material.YELLOW_TERRACOTTA);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&e左にベースアイテムをセット、右に要求する予定のアイテムをセット&c&l✖"));
        barrier.setItemMeta(meta);

        return barrier;
    }

    @NotNull
    public static ItemStack getYellowCreate() {
        ItemStack barrier = new ItemStack(Material.YELLOW_STAINED_GLASS_PANE);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&eアイテムがセットされていないため生成できません。&c&l✖"));
        barrier.setItemMeta(meta);

        return barrier;
    }

    @NotNull
    public static ItemStack getGreenSet() {
        ItemStack barrier = new ItemStack(Material.GREEN_TERRACOTTA);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&a左にベースアイテムをセット、右に要求する予定のアイテムをセット&a&l○"));
        barrier.setItemMeta(meta);

        return barrier;
    }

    @NotNull
    public static ItemStack getGreenCreate() {
        ItemStack barrier = new ItemStack(Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta meta = barrier.getItemMeta();
        meta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize("&aここをクリックで、生成可能です。"));
        barrier.setItemMeta(meta);

        return barrier;
    }
}
