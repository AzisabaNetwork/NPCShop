package net.azisaba.plugin.npcshop;

import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.utils.Keys;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class NPCEntity {

    @NotNull
    private NPCShop getPlugin() {
        return JavaPlugin.getPlugin(NPCShop.class);
    }

    public void spawn(@NotNull Location loc, EntityType type) {
        loc.getNearbyEntities(0.25, 0.25, 0.25)
                .stream()
                .filter(e -> e.getPersistentDataContainer().has(Keys.SHOP_KEEPER, PersistentDataType.STRING))
                .forEach(Entity::remove);
        npc(loc, type);
    }

    private void npc(@NotNull Location loc, EntityType type) {
        String name = getPlugin().getConfig().getString("EntityOptions.DefaultsDisplayName", "&b&lNPCShop");

        DisguiseType t = DisguiseType.getType(type);
        ArmorStand entity = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        entity.setSilent(true);
        entity.setGravity(false);
        entity.setInvisible(true);
        entity.setCollidable(false);
        entity.setCustomNameVisible(true);
        entity.setAI(false);
        entity.setRemoveWhenFarAway(true);
        entity.getPersistentDataContainer().set(Keys.SHOP_KEEPER, PersistentDataType.STRING, "true");
        entity.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));

        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attr != null) {
            attr.addModifier(new AttributeModifier(UUID.randomUUID(), "npc_protect", 100.0, AttributeModifier.Operation.ADD_NUMBER));
        }

        MobDisguise mob = new MobDisguise(t).setEntity(entity);
        mob.startDisguise();
    }
}
