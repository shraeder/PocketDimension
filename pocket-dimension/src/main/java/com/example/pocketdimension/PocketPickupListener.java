package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PocketPickupListener implements Listener {
    private final PocketPlugin plugin;
    private final List<Material> trackedMaterials = Arrays.asList(
        Material.COBBLESTONE, Material.COBBLED_DEEPSLATE, Material.DIORITE,
        Material.ANDESITE, Material.GRANITE, Material.GRAVEL, Material.DIRT,
        Material.SAND, Material.NETHERRACK
    );

    public PocketPickupListener(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();
        Item itemEntity = event.getItem();
        ItemStack itemStack = itemEntity.getItemStack();
        Material material = itemStack.getType();

        if (!trackedMaterials.contains(material)) return;

        // Check if the player has a Dimensional Pocket in inventory
        boolean hasPocket = false;
        for (ItemStack invItem : player.getInventory().getContents()) {
            if (isPocketItem(invItem)) {
                hasPocket = true;
                break;
            }
        }

        if (!hasPocket) return;

        // Store item and cancel normal pickup
        UUID uuid = player.getUniqueId();
        int amount = itemStack.getAmount();
        plugin.getStorageManager().addAmount(uuid, material.name(), amount);

        event.setCancelled(true);
        itemEntity.remove();

        // Play the normal pickup sound
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1.0f, 1.0f);
    }

    private boolean isPocketItem(ItemStack item) {
        if (item == null || item.getType() != Material.GLASS_BOTTLE) return false;
        if (!item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        return "§bDimensional Pocket".equals(meta.getDisplayName());
    }
}
