package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PocketGhostItemListener implements Listener {
    private final PocketPlugin plugin;

    public PocketGhostItemListener(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack inHand = event.getItemInHand();

        if (!plugin.getPocketGhostItem().isGhost(inHand)) return;

        UUID playerId = player.getUniqueId();
        Material selected = plugin.getPocketModeManager().getPlacementMaterial(playerId);
        Material ghostMat = plugin.getPocketGhostItem().getGhostMaterial(inHand);

        // Must match the selected material.
        if (selected == null || ghostMat == null || selected != ghostMat) {
            event.setCancelled(true);
            plugin.disablePlacementMode(player);
            return;
        }

        int stored = plugin.getStorageManager().getAmount(playerId, selected.name());
        if (stored <= 0) {
            event.setCancelled(true);
            plugin.disablePlacementMode(player);
            player.sendMessage("§cYou have no " + selected.name() + " stored.");
            return;
        }

        // Let vanilla place the block, then decrement our storage.
        plugin.getStorageManager().addAmount(playerId, selected.name(), -1);

        // Resync the ghost stack amount/lore immediately + next tick.
        // With stack size 1, vanilla will consume the item, so we must restore it.
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.refreshGhostItem(player));
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> plugin.refreshGhostItem(player), 1L);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack drop = event.getItemDrop().getItemStack();
        if (plugin.getPocketGhostItem().isGhost(drop)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        if (plugin.getPocketGhostItem().isGhost(current) || plugin.getPocketGhostItem().isGhost(cursor)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        for (ItemStack stack : event.getNewItems().values()) {
            if (plugin.getPocketGhostItem().isGhost(stack)) {
                event.setCancelled(true);
                return;
            }
        }

        // Also block if dragging over a slot that currently contains a ghost item.
        for (Integer rawSlot : event.getRawSlots()) {
            ItemStack existing = event.getView().getItem(rawSlot);
            if (plugin.getPocketGhostItem().isGhost(existing)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onSneakLeftClickDisable(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getPlayer().isSneaking()) return;

        String action = event.getAction().toString();
        if (!action.contains("LEFT_CLICK")) return;

        ItemStack inHand = event.getPlayer().getInventory().getItemInMainHand();
        if (!plugin.getPocketGhostItem().isGhost(inHand)) return;

        event.setCancelled(true);
        plugin.disablePlacementMode(event.getPlayer());
        event.getPlayer().sendMessage("§aDimensional Pocket placement mode disabled.");
        event.getPlayer().sendMessage("§7Shift-left-click again to open the pocket GUI.");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneakLeftClickDisableAir(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!plugin.getPocketGhostItem().isGhost(inHand)) return;

        plugin.disablePlacementMode(player);
        player.sendMessage("§aDimensional Pocket placement mode disabled.");
        player.sendMessage("§7Shift-left-click again to open the pocket GUI.");
    }
}
