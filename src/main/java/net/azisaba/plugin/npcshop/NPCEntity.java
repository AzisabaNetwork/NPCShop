package net.azisaba.plugin.npcshop;

import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;
import net.azisaba.plugin.NPCShop;
import net.azisaba.plugin.data.database.DBShop;
import net.azisaba.plugin.utils.Keys;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.CreatureSpawnEvent;
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
        for (LivingEntity l : loc.getNearbyLivingEntities(0.25, 0.25, 0.25).stream().toList()) {
            if (l.getPersistentDataContainer().has(Keys.SHOP_KEEPER, PersistentDataType.STRING)) {
                update(l, type);
                return;
            }
        }
        npc(loc, type);
    }

    public void update(LivingEntity living, EntityType type) {
        DisguiseType t = DisguiseType.getType(type);
        if (DisguiseAPI.isDisguised(living)) {
            living.getWorld().getPlayers().forEach(p -> DisguiseAPI.getDisguise(p, living));
        } else {
            MobDisguise mob = new MobDisguise(t).setEntity(living);
            mob.startDisguise();
        }
    }

    private void npc(@NotNull Location loc, EntityType type) {
        String name = getPlugin().getConfig().getString("EntityOptions.DefaultsDisplayName", "&b&lNPCShop");
        if (DBShop.getShopEntity().containsKey(ShopLocation.adapt(loc))) {
            ShopEntity entity = DBShop.getShopEntity().get(ShopLocation.adapt(loc));
            if (entity.name() != null) {
                name = entity.name();
            }
        }
        DisguiseType t = DisguiseType.getType(type);
        Villager entity = (Villager) loc.getWorld().spawnEntity(loc, EntityType.VILLAGER, CreatureSpawnEvent.SpawnReason.COMMAND);

        entity.setSilent(true);
        entity.setGravity(false);
        entity.setInvisible(false);
        entity.setCollidable(true);
        entity.setAI(false);
        entity.setRemoveWhenFarAway(true);
        entity.setCustomNameVisible(false);
        entity.setRotation((float) 0, 0);
        entity.customName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        entity.getPersistentDataContainer().set(Keys.SHOP_KEEPER, PersistentDataType.STRING, "true");
        entity.getPersistentDataContainer().set(Keys.SHOP_TYPE, PersistentDataType.STRING, type.name());

        AttributeInstance attr = entity.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (attr != null) {
            attr.addModifier(new AttributeModifier(UUID.randomUUID(), "npc_protect", 100.0, AttributeModifier.Operation.ADD_NUMBER));
        }

        MobDisguise mob = new MobDisguise(t).setEntity(entity);
        mob.startDisguise();
    }

    public static void despawn(@NotNull ShopLocation l) {
        Location loc = new Location(l.w(), l.x(), l.y(), l.z());
        for (LivingEntity living : loc.getNearbyLivingEntities(0.25, 0.25, 0.25).stream().toList()) {
            if (living.getPersistentDataContainer().has(Keys.SHOP_KEEPER, PersistentDataType.STRING)) {
                living.remove();
            }
        }
    }
}
