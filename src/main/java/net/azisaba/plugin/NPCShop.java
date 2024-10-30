package net.azisaba.plugin;

import com.github.bea4dev.artgui.ArtGUI;
import net.azisaba.plugin.commands.ShopCommand;
import net.azisaba.plugin.database.DBConnector;
import net.azisaba.plugin.database.DBShop;
import net.azisaba.plugin.database.SaveDB;
import net.azisaba.plugin.listeners.InventoryListener;
import net.azisaba.plugin.listeners.PlayerListener;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NPCShop extends JavaPlugin implements Main, Task {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerListeners();
        registerCommands();
        registerClockTimer();
    }

    @Override
    public void onDisable() {

        DBConnector.close();
    }

    @Override
    public void registerCommands() {
        Objects.requireNonNull(getCommand("shop")).setExecutor(new ShopCommand());
    }

    @Override
    public void registerListeners() {
        new PlayerListener().initialize(this);
        new InventoryListener().initialize(this);
    }

    @Override
    public void registerClockTimer() {
        runAsyncDelayed(() -> new DBShop().load(), 100);
        runSyncTimer(() -> new SaveDB().save(false), 18000, 18000);
    }

    @NotNull
    @Contract(" -> new")
    @Override
    public ArtGUI getArtGUI() {return new ArtGUI(this);}

    @Override
    public void runAsync(Runnable runnable) {Bukkit.getScheduler().runTaskAsynchronously(this, runnable);}

    @Override
    public void runSync(Runnable runnable) {Bukkit.getScheduler().runTask(this, runnable);}

    @Override
    public void runSyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLater(this, runnable, delay);}

    @Override
    public void runAsyncDelayed(Runnable runnable, long delay) {Bukkit.getScheduler().runTaskLaterAsynchronously(this, runnable, delay);}

    @Override
    public void runSyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimer(this, runnable, delay, loop);}

    @Override
    public void runAsyncTimer(Runnable runnable, long delay, long loop) {Bukkit.getScheduler().runTaskTimerAsynchronously(this, runnable, delay, loop);}

}
