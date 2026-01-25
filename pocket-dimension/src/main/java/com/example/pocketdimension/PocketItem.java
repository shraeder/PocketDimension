package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

public final class PocketItem {
    public static final Material MATERIAL = Material.BUNDLE;
    public static final String DISPLAY_NAME = "§bDimensional Pocket";
    public static final String LORE = "§7Shift-left-click while holding to open";

    private PocketItem() {
    }

    public static ItemStack create() {
        ItemStack item = new ItemStack(MATERIAL);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(DISPLAY_NAME);
            meta.setLore(Collections.singletonList(LORE));

            // Bundles add extra vanilla tooltip lines (e.g., "Can hold a mixed stack of items", "Empty").
            // This flag hides those extra item-specific lines while keeping our custom name/lore.
            applyHideAdditionalTooltip(meta);

            item.setItemMeta(meta);
        }
        return item;
    }

    private static void applyHideAdditionalTooltip(ItemMeta meta) {
        try {
            ItemFlag flag = ItemFlag.valueOf("HIDE_ADDITIONAL_TOOLTIP");
            meta.addItemFlags(flag);
        } catch (IllegalArgumentException ignored) {
            // Server API doesn't expose this flag (or uses a different name) — nothing we can do here.
        }
    }

    public static boolean isPocketItem(ItemStack item) {
        if (item == null || item.getType() != MATERIAL) return false;
        if (!item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        return meta != null && DISPLAY_NAME.equals(meta.getDisplayName());
    }
}
