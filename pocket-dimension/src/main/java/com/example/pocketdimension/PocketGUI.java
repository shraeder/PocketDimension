package com.example.pocketdimension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class PocketGUI implements Listener {
    private final PocketPlugin plugin;
    private final Map<Integer, Material> slotMaterialMap;

    private final List<Material> trackedMaterials = Arrays.asList(
        Material.COBBLESTONE, Material.COBBLED_DEEPSLATE, Material.DIORITE,
        Material.ANDESITE, Material.GRANITE, Material.GRAVEL, Material.DIRT,
        Material.SAND, Material.NETHERRACK
    );

    public PocketGUI(PocketPlugin plugin) {
        this.plugin = plugin;
        this.slotMaterialMap = new HashMap<>();
        for (int i = 0; i < trackedMaterials.size(); i++) {
            slotMaterialMap.put(i, trackedMaterials.get(i));
        }
    }

    @EventHandler
    public void onUsePocket(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check shift + left click
        if (!player.isSneaking() || !event.getAction().toString().contains("LEFT_CLICK")) return;

        ItemStack item = event.getItem();
        if (item != null && item.getType() == Material.GLASS_BOTTLE &&
            item.hasItemMeta() &&
            "§bDimensional Pocket".equals(item.getItemMeta().getDisplayName())) {
        
            event.setCancelled(true); // Stop breaking blocks or doing anything else
            openPocket(player);
        }
    }   

    private void openPocket(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, "Dimensional Pocket");
        for (int i = 0; i < 9; i++) {
            Material mat = slotMaterialMap.get(i);
            int count = plugin.getStorageManager().getAmount(player.getUniqueId(), mat.name());

            ItemStack icon = new ItemStack(mat);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName("§e" + mat.name());
            meta.setLore(Collections.singletonList("§7Stored: " + count));
            icon.setItemMeta(meta);

            inv.setItem(i, icon);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!"Dimensional Pocket".equals(event.getView().getTitle())) return;

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        int slot = event.getRawSlot();

        if (slot >= 0 && slot < 9) {
            Material mat = slotMaterialMap.get(slot);
            UUID uuid = player.getUniqueId();
            int current = plugin.getStorageManager().getAmount(uuid, mat.name());

            if (current > 0 && player.getInventory().firstEmpty() != -1) {
                int withdrawAmount = Math.min(64, current);
                player.getInventory().addItem(new ItemStack(mat, withdrawAmount));
                plugin.getStorageManager().addAmount(uuid, mat.name(), -withdrawAmount);
                openPocket(player);
            }
        }
    }
}
