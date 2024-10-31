package net.azisaba.plugin.npcshop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

public record ShopEntity(String name, EntityType type) {

    @NotNull
    public static String getString(Component comp) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(comp);
    }
}
