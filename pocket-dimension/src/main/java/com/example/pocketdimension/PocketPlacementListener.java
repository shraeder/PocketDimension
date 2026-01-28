package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.SoundGroup;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PocketPlacementListener implements Listener {
    private final PocketPlugin plugin;
    private final Map<UUID, Long> lastErrorMessageMillis = new ConcurrentHashMap<>();

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

        UUID playerId = player.getUniqueId();
        Material selected = plugin.getPocketModeManager().getPlacementMaterial(playerId);
        if (selected == null) return;

        if (!selected.isBlock() || selected == Material.AIR || selected == Material.CAVE_AIR || selected == Material.VOID_AIR) {
            sendRateLimited(player, "§cThat material can't be placed as a block.");
            return;
        }

        int stored = plugin.getStorageManager().getAmount(player.getUniqueId(), selected.name());
        if (stored <= 0) {
            sendRateLimited(player, "§cYou have no " + selected.name() + " stored.");
            return;
        }

        // In placement mode, right-click should not open containers / toggle blocks.
        event.setUseInteractedBlock(Event.Result.DENY);
        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);

        // Rely on vanilla's own repeat behavior (client sends repeated RIGHT_CLICK_BLOCK packets while held).
        attemptPlaceOnce(player, event);
    }

    private void attemptPlaceOnce(Player player, PlayerInteractEvent event) {
        UUID playerId = player.getUniqueId();
        Material selected = plugin.getPocketModeManager().getPlacementMaterial(playerId);
        if (selected == null) return;

        if (!selected.isBlock() || selected == Material.AIR || selected == Material.CAVE_AIR || selected == Material.VOID_AIR) {
            sendRateLimited(player, "§cThat material can't be placed as a block.");
            return;
        }

        int stored = plugin.getStorageManager().getAmount(playerId, selected.name());
        if (stored <= 0) {
            sendRateLimited(player, "§cYou have no " + selected.name() + " stored.");
            return;
        }

        if (event.getClickedBlock() == null || event.getBlockFace() == null) return;

        Block clicked = event.getClickedBlock();
        Block target = clicked.getRelative(event.getBlockFace());
        if (!target.getType().isAir()) return;

        // Respect protections/region plugins by firing a BlockPlaceEvent.
        BlockState replacedState = target.getState();
        ItemStack fakeInHand = new ItemStack(selected, 1);
        BlockPlaceEvent placeEvent = new BlockPlaceEvent(target, replacedState, clicked, fakeInHand, player, true, EquipmentSlot.HAND);
        plugin.getServer().getPluginManager().callEvent(placeEvent);
        if (placeEvent.isCancelled() || !placeEvent.canBuild()) return;

        target.setType(selected, true);
        plugin.getStorageManager().addAmount(playerId, selected.name(), -1);

        playPlaceSound(target);
    }

    private void sendRateLimited(Player player, String message) {
        long now = System.currentTimeMillis();
        long last = lastErrorMessageMillis.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 750) return;
        lastErrorMessageMillis.put(player.getUniqueId(), now);
        player.sendMessage(message);
    }

    private void playPlaceSound(Block placedBlock) {
        try {
            SoundGroup group = placedBlock.getBlockData().getSoundGroup();
            Sound sound = group.getPlaceSound();
            placedBlock.getWorld().playSound(
                placedBlock.getLocation().add(0.5, 0.5, 0.5),
                sound,
                SoundCategory.BLOCKS,
                group.getVolume(),
                group.getPitch()
            );
        } catch (Throwable ignored) {
            // Older API or unexpected edge case; fail silently.
        }
    }
}
