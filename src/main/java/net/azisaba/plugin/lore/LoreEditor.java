package net.azisaba.plugin.lore;

import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.SimplePacketListenerAbstract;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.recipe.data.MerchantOffer;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientCreativeInventoryAction;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerMerchantOffers;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSetSlot;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerWindowItems;
import io.github.retrooper.packetevents.util.SpigotConversionUtil;
import net.azisaba.plugin.event.ItemLoreEditEvent;
import net.azisaba.plugin.utils.Util;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class LoreEditor extends SimplePacketListenerAbstract {

    public LoreEditor() {
        super(PacketListenerPriority.LOWEST);
    }

    @Override
    public void onPacketPlayReceive(@NotNull PacketPlayReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CREATIVE_INVENTORY_ACTION) {

            WrapperPlayClientCreativeInventoryAction packet = new WrapperPlayClientCreativeInventoryAction(event);
            ItemStack item = SpigotConversionUtil.toBukkitItemStack(packet.getItemStack());
            if (item != null) {
                packet.setItemStack(SpigotConversionUtil.fromBukkitItemStack(removeCustomLore(item)));
                packet.write();
            }

        } else if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {

            WrapperPlayClientClickWindow packet = new WrapperPlayClientClickWindow(event);
            Map<Integer, com.github.retrooper.packetevents.protocol.item.ItemStack> map = packet.getSlots().orElse(null);
            if (map != null) {
                Map<Integer, com.github.retrooper.packetevents.protocol.item.ItemStack> set = new HashMap<>();
                for (Map.Entry<Integer, com.github.retrooper.packetevents.protocol.item.ItemStack> entry : map.entrySet()) {
                    if (entry.getValue() == null) {
                        set.put(entry.getKey(), null);
                    } else {
                        ItemStack pass = removeCustomLore(SpigotConversionUtil.toBukkitItemStack(entry.getValue()));
                        set.put(entry.getKey(), SpigotConversionUtil.fromBukkitItemStack(pass));
                    }
                }
                Optional<Map<Integer, com.github.retrooper.packetevents.protocol.item.ItemStack>> op = Optional.of(set);
                packet.setSlots(op);
                packet.write();
            }
        }
    }

    @Override
    public void onPacketPlaySend(@NotNull PacketPlaySendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.SET_SLOT) {

            WrapperPlayServerSetSlot packet = new WrapperPlayServerSetSlot(event);
            packet.setItem(SpigotConversionUtil.fromBukkitItemStack(setCustomLore(SpigotConversionUtil.toBukkitItemStack(packet.getItem()))));
            packet.write();

        } else if (event.getPacketType() == PacketType.Play.Server.WINDOW_ITEMS) {

            WrapperPlayServerWindowItems packet = new WrapperPlayServerWindowItems(event);
            List<com.github.retrooper.packetevents.protocol.item.ItemStack> list = new ArrayList<>();
            for (com.github.retrooper.packetevents.protocol.item.ItemStack stack : packet.getItems()) {
                list.add(SpigotConversionUtil.fromBukkitItemStack(setCustomLore(SpigotConversionUtil.toBukkitItemStack(stack))));
            }
            packet.setItems(list);
            packet.getCarriedItem().ifPresent(get ->
                    packet.setCarriedItem(SpigotConversionUtil.fromBukkitItemStack(setCustomLore(SpigotConversionUtil.toBukkitItemStack(get)))));
            packet.write();

        } else if (event.getPacketType() == PacketType.Play.Server.MERCHANT_OFFERS) {

            WrapperPlayServerMerchantOffers packet = new WrapperPlayServerMerchantOffers(event);
            List<MerchantOffer> offers = new ArrayList<>();
            for (MerchantOffer offer : packet.getMerchantOffers()) {
                com.github.retrooper.packetevents.protocol.item.ItemStack stack = offer.getOutputItem();
                if (stack != null) {
                    com.github.retrooper.packetevents.protocol.item.ItemStack set = SpigotConversionUtil.fromBukkitItemStack(setCustomLore(SpigotConversionUtil.toBukkitItemStack(stack)));
                    offers.add(MerchantOffer.of(offer.getFirstInputItem(), offer.getSecondInputItem(), set, offer.getUses(), offer.getMaxUses(), offer.getXp(), offer.getSpecialPrice(), offer.getPriceMultiplier(), offer.getDemand()));
                }
            }
            packet.setMerchantOffers(offers);
            packet.write();
        }
    }

    public ItemStack setCustomLore(ItemStack item) {
        if (item == null || item.getType().isAir()) return item;
        ItemStack copy = new ItemStack(item);
        ItemMeta meta = copy.getItemMeta();
        if (meta == null) return item;

        List<String> list = new ArrayList<>();
        ItemLoreEditEvent event = new ItemLoreEditEvent(list, item);
        if (event.callEvent()) {
            if (event.getLore().isEmpty()) return event.getItem();
            return LoreHandler.setTagInLore(event.getItem(), event.getLore());
        }
        return copy;
    }

    public ItemStack removeCustomLore(ItemStack item) {
        if (item == null || item.getType().isAir() || !item.hasItemMeta()) return item;
        String mmid = Util.getMythicID(item);
        if (mmid == null) return item;

        return LoreHandler.removeTagInLore(item, Util.getMythicItemStack(mmid));
    }
}
