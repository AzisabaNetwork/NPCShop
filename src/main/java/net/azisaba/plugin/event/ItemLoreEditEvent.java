package net.azisaba.plugin.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ItemLoreEditEvent extends Event implements Cancellable {

    private static final HandlerList handler = new HandlerList();
    private final List<String> lore;
    private final ItemStack item;
    private boolean cancelled = false;

    public ItemLoreEditEvent(List<String> lore, ItemStack item) {
        super(true);
        this.lore = lore;
        this.item = item;
    }

    public List<String> getLore() {
        return lore;
    }

    public void addLore(List<String> lore) {
        this.lore.addAll(lore);
    }

    public ItemStack getItem() {
        return item;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handler;
    }

    @SuppressWarnings("unused")
    public static HandlerList getHandlerList() {
        return handler;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
