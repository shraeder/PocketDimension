package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class PocketGhostItem {
    private static final String DISPLAY_PREFIX = "§bPocket Block: §f";

    private final NamespacedKey ghostKey;
    private final NamespacedKey materialKey;

    public PocketGhostItem(PocketPlugin plugin) {
        this.ghostKey = new NamespacedKey(plugin, "ghost_block");
        this.materialKey = new NamespacedKey(plugin, "ghost_material");
    }

    public ItemStack create(Material material, int shownAmount, int storedAmount) {
        ItemStack item = new ItemStack(material, Math.max(1, Math.min(64, shownAmount)));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(DISPLAY_PREFIX + material.name());
        List<String> lore = new ArrayList<>();
        lore.add("§7From your Dimensional Pocket");
        lore.add("§7Stored: " + storedAmount);
        lore.add("§7Shift-left-click: disable placement");
        meta.setLore(lore);

        // Enchant glint to distinguish it visually.
        try {
            org.bukkit.enchantments.Enchantment glint = org.bukkit.enchantments.Enchantment.getByKey(
                org.bukkit.NamespacedKey.minecraft("unbreaking")
            );
            if (glint != null) {
                meta.addEnchant(glint, 1, true);
            }
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } catch (Throwable ignored) {
            // Safe fallback for older APIs.
        }

        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(ghostKey, PersistentDataType.BYTE, (byte) 1);
        pdc.set(materialKey, PersistentDataType.STRING, material.name());

        item.setItemMeta(meta);
        return item;
    }

    public boolean isGhost(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Byte val = meta.getPersistentDataContainer().get(ghostKey, PersistentDataType.BYTE);
        return val != null && val == (byte) 1;
    }

    public Material getGhostMaterial(ItemStack item) {
        if (!isGhost(item)) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        String matName = meta.getPersistentDataContainer().get(materialKey, PersistentDataType.STRING);
        if (matName == null) return null;
        return Material.matchMaterial(matName);
    }
}
