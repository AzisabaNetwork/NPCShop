package net.azisaba.plugin.utils;

import com.google.common.collect.Multimap;
import net.azisaba.plugin.NPCShop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class CoolTime {

    public static boolean isCoolTime(Class<?> clazz, @NotNull Entity entity, @NotNull Multimap<Class<?>, UUID> multimap) {
        if (multimap.containsEntry(clazz, entity.getUniqueId())) {
            entity.sendMessage(Component.text("クールダウン中です。", NamedTextColor.RED));
            return true;
        } else {
            return false;
        }
    }

    public static void setCoolTime(Class<?> clazz, @NotNull Entity entity, @NotNull Multimap<Class<?>, UUID> multimap, long tick) {
        multimap.put(clazz, entity.getUniqueId());
        JavaPlugin.getPlugin(NPCShop.class).runAsyncDelayed(()-> multimap.remove(clazz, entity.getUniqueId()), tick);
    }
}
