package com.example.pocketdimension;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public class PocketItemNormalizeListener implements Listener {
    private final PocketPlugin plugin;

    public PocketItemNormalizeListener(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Run a tick later to avoid edge cases with inventory initialization.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            ItemStack[] contents = event.getPlayer().getInventory().getContents();
            boolean changedAny = false;

            for (int i = 0; i < contents.length; i++) {
                ItemStack item = contents[i];
                if (!PocketItem.isPocketItem(item)) continue;

                changedAny |= normalize(item);
            }

            if (changedAny) {
                event.getPlayer().getInventory().setContents(contents);
            }
        }, 1L);
    }

    private boolean normalize(ItemStack item) {
        boolean changed = false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        // Ensure the hide-additional-tooltip flag is present.
        try {
            ItemFlag flag = ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP");
            if (!meta.getItemFlags().contains(flag)) {
                meta.addItemFlags(flag);
                changed = true;
            }
        } catch (IllegalArgumentException ignored) {
            // Not supported by this API.
        }

        // Ensure bundle has no contents.
        if (meta instanceof BundleMeta) {
            BundleMeta bundleMeta = (BundleMeta) meta;
            if (!bundleMeta.getItems().isEmpty()) {
                bundleMeta.setItems(Collections.emptyList());
                meta = bundleMeta;
                changed = true;
            }
        }

        if (changed) {
            item.setItemMeta(meta);
        }

        return changed;
    }
}
