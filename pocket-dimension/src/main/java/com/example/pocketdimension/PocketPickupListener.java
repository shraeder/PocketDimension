package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.UUID;

@SuppressWarnings("deprecation")
public class PocketPickupListener implements Listener {
    private final PocketPlugin plugin;
    private final Set<Material> trackedMaterials;

    public PocketPickupListener(PocketPlugin plugin) {
        this.plugin = plugin;
        this.trackedMaterials = new HashSet<>();

        // Load tracked materials from gui-items.yml so pickup-storage matches the GUI configuration
        try {
            File cfgFile = new File(plugin.getDataFolder(), "gui-items.yml");
            if (!cfgFile.exists()) {
                plugin.saveResource("gui-items.yml", false);
            }

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
            List<String> items = cfg.getStringList("items");
            for (String matName : items) {
                if (matName == null) continue;
                Material mat = Material.matchMaterial(matName);
                if (mat != null) trackedMaterials.add(mat);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load gui-items.yml for pickup storage: " + e.getMessage());
        }

        // Fallback to original defaults if config is empty/invalid
        if (trackedMaterials.isEmpty()) {
            trackedMaterials.addAll(Arrays.asList(
                Material.COBBLESTONE, Material.COBBLED_DEEPSLATE, Material.DIORITE,
                Material.ANDESITE, Material.GRANITE, Material.GRAVEL, Material.DIRT,
                Material.SAND, Material.NETHERRACK
            ));
        }
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
            if (PocketItem.isPocketItem(invItem)) {
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

    
}
