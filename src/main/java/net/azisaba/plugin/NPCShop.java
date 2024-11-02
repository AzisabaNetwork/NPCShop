package net.azisaba.plugin;

import com.github.bea4dev.artgui.ArtGUI;
import net.azisaba.plugin.commands.ShopCommand;
import net.azisaba.plugin.data.SaveDB;
import net.azisaba.plugin.data.database.DBConnector;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.listeners.EntityListener;
import net.azisaba.plugin.listeners.InventoryListener;
import net.azisaba.plugin.listeners.ItemListener;
import net.azisaba.plugin.listeners.PlayerListener;
import net.azisaba.plugin.npcshop.NPCEntity;
import net.azisaba.plugin.npcshop.ShopLocation;
import net.azisaba.plugin.utils.Util;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class NPCShop extends JavaPlugin implements Main, Task {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        registerClockTimer();
        registerListeners();
        registerCommands();
    }

    @Override
    public void onDisable() {
        for  (ShopLocation loc : DBShop.getShopEntity().keySet()) {
            NPCEntity.despawn(loc);
        }
        new SaveDB().save(true);
        DBConnector.close();
    }

    @Override
    public void registerCommands() {
        Objects.requireNonNull(getCommand("npcshop")).setExecutor(new ShopCommand());
    }

    @Override
    public void registerListeners() {
        new PlayerListener().initialize(this);
        new InventoryListener().initialize(this);
        new EntityListener().initialize(this);
        new ItemListener().initialize(this);
    }

    @Override
    public void registerClockTimer() {
        if (!getConfig().getBoolean("Database.use", false)) return;
        runAsync(() -> new DBConnector().initialize(this));
        runAsyncDelayed(() -> new DBShop().load(), 20);
        runSyncTimer(() -> new SaveDB().save(false), 18000, 18000);

        runSyncDelayed(() -> DBShop.getShopEntity().forEach((key, value) -> {
            if (value.type() == null) return;
            NPCEntity.despawn(key);
        }), 30);
        runAsyncTimer(() -> {
            if (Util.isEnabled()) return;
            DBShop.getShopEntity().forEach((key, value) -> {
                if (value.type() == null) return;
                runSync(() -> new NPCEntity().spawn(ShopLocation.adapt(key), value.type()));
            });
        }, 50, 300);
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
