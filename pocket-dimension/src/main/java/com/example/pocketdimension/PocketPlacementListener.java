package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class PocketPlacementListener implements Listener {
    private final PocketPlugin plugin;

    public PocketPlacementListener(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent event) {
        // Avoid firing twice (main hand + offhand).
        if (event.getHand() != EquipmentSlot.HAND) return;

        Action action = event.getAction();
        if (action != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        // Only allow placement when holding a pocket item.
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!PocketItem.isPocketItem(inHand)) return;

        // Sneak-right-click is reserved for "deselect" (handled by PocketGUI listener).
        if (player.isSneaking()) return;

        Material selected = plugin.getPocketModeManager().getPlacementMaterial(player.getUniqueId());
        if (selected == null) return;

        if (!selected.isBlock() || selected == Material.AIR || selected == Material.CAVE_AIR || selected == Material.VOID_AIR) {
            player.sendMessage("§cThat material can't be placed as a block.");
            return;
        }

        int stored = plugin.getStorageManager().getAmount(player.getUniqueId(), selected.name());
        if (stored <= 0) {
            player.sendMessage("§cYou have no " + selected.name() + " stored.");
            return;
        }

        Block clicked = event.getClickedBlock();
        if (clicked == null) return;

        Block target = clicked.getRelative(event.getBlockFace());
        if (!target.getType().isAir()) {
            player.sendMessage("§cNo space to place that block.");
            return;
        }

        // Cancel vanilla interaction (e.g., opening chests) and place from the pocket.
        event.setCancelled(true);

        target.setType(selected, true);
        plugin.getStorageManager().addAmount(player.getUniqueId(), selected.name(), -1);
    }
}
