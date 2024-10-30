package net.azisaba.plugin.utils.shop;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class ShopItemBuilder {

    private final Material material;

    public ShopItemBuilder(Material material) {
        this.material = material;
    }

    private String name = null;

    private String[] lore = null;

    public ShopItemBuilder name(String name) {
        this.name = name;
        return this;
    }

    public ShopItemBuilder lore(String... lore) {
        this.lore = lore;
        return this;
    }

    public ItemStack build(){
        ItemStack itemStack = new ItemStack(material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(name != null){
            itemMeta.displayName(LegacyComponentSerializer.legacyAmpersand().deserialize(name));
        }

        if(lore != null){
            List<Component> loreList = new ArrayList<>();
            for(String line : lore){
                loreList.add(LegacyComponentSerializer.legacyAmpersand().deserialize(line));
            }
            itemMeta.lore(loreList);
        }

        itemMeta.getPersistentDataContainer().set(ShopKeys.SHOP_ITEMS, PersistentDataType.STRING, "true");
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
