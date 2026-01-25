package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

import java.util.Collections;

public class PocketItemProtectionListener implements Listener {

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        boolean currentPocket = PocketItem.isPocketItem(current);
        boolean cursorPocket = PocketItem.isPocketItem(cursor);
        if (!currentPocket && !cursorPocket) return;

        if (currentPocket) sanitizeBundleMeta(current);
        if (cursorPocket) sanitizeBundleMeta(cursor);

        boolean cursorHasItem = cursor != null && cursor.getType() != Material.AIR;
        boolean currentHasItem = current != null && current.getType() != Material.AIR;

        // Case A: Player is clicking the pocket bundle while holding something else -> block insertion.
        if (currentPocket && cursorHasItem && !cursorPocket) {
            event.setCancelled(true);
        }

        // Case B: Player is holding the pocket bundle on the cursor and clicking other items -> block insertion.
        // Allow placing the pocket bundle into an empty slot.
        if (cursorPocket) {
            if (!(isPlacingCursorIntoEmptySlot(event.getAction(), currentHasItem))) {
                event.setCancelled(true);
            }
        }

        // Bundle insertion/removal logic can be handled in special ways; enforce empty contents next tick.
        Bukkit.getScheduler().runTask(PocketPlugin.getInstance(), () -> {
            ItemStack afterCurrent = event.getCurrentItem();
            ItemStack afterCursor = event.getCursor();
            if (PocketItem.isPocketItem(afterCurrent)) sanitizeBundleMeta(afterCurrent);
            if (PocketItem.isPocketItem(afterCursor)) sanitizeBundleMeta(afterCursor);
        });
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        // Prevent dragging items over the pocket item slot (can trigger bundle insertion).
        for (Integer rawSlot : event.getRawSlots()) {
            ItemStack existing = event.getView().getItem(rawSlot);
            if (PocketItem.isPocketItem(existing)) {
                sanitizeBundleMeta(existing);
                event.setCancelled(true);
                return;
            }
        }
    }

    private boolean isPlacingCursorIntoEmptySlot(InventoryAction action, boolean currentHasItem) {
        if (currentHasItem) return false;
        switch (action) {
            case PLACE_ALL:
            case PLACE_ONE:
            case PLACE_SOME:
                return true;
            default:
                return false;
        }
    }

    private void sanitizeBundleMeta(ItemStack pocketItem) {
        if (pocketItem == null) return;
        if (!(pocketItem.getItemMeta() instanceof BundleMeta)) return;

        BundleMeta meta = (BundleMeta) pocketItem.getItemMeta();
        if (meta != null && !meta.getItems().isEmpty()) {
            meta.setItems(Collections.emptyList());
            pocketItem.setItemMeta(meta);
        }
    }
}
