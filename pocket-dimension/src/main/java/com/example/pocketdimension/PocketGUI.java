package com.example.pocketdimension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PocketGUI implements Listener {
    private final PocketPlugin plugin;
    private final PocketModeManager modeManager;
    private final Map<Integer, Material> slotMaterialMap;
    private List<Material> trackedMaterials;

    public PocketGUI(PocketPlugin plugin) {
        this.plugin = plugin;
        this.modeManager = plugin.getPocketModeManager();
        this.slotMaterialMap = new HashMap<>();
        this.trackedMaterials = new ArrayList<>();

        // Load gui-items.yml from plugin data folder (saved by PocketPlugin)
        try {
            File cfgFile = new File(plugin.getDataFolder(), "gui-items.yml");
            if (!cfgFile.exists()) {
                plugin.saveResource("gui-items.yml", false);
            }

            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(cfgFile);
            List<String> items = cfg.getStringList("items");

            for (int i = 0; i < items.size(); i++) {
                String matName = items.get(i);
                if (matName == null) continue;
                Material mat = Material.matchMaterial(matName);
                if (mat == null) continue;

                slotMaterialMap.put(i, mat);
                trackedMaterials.add(mat);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load gui-items.yml: " + e.getMessage());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onUsePocket(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        // Avoid firing twice (main hand + offhand).
        if (event.getHand() != EquipmentSlot.HAND) return;

        // event.getItem() can be null for some actions; use main hand directly.
        ItemStack item = player.getInventory().getItemInMainHand();
        if (PocketItem.isPocketItem(item)) {
            String action = event.getAction().toString();

            // Sneak-left-click while holding: open GUI (and reset state back to default).
            if (action.contains("LEFT_CLICK")) {
                event.setCancelled(true);
                plugin.disablePlacementMode(player);
                openPocket(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSneakLeftClickAirOpen(PlayerAnimationEvent event) {
        Player player = event.getPlayer();
        if (!player.isSneaking()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!PocketItem.isPocketItem(item)) return;

        // If a block is being targeted, PlayerInteractEvent (LEFT_CLICK_BLOCK) will handle it.
        // This is the fallback for left-click air.
        if (player.getTargetBlockExact(5) != null) return;

        plugin.disablePlacementMode(player);
        openPocket(player);
    }

    private void openPocket(Player player) {
        int itemCount = slotMaterialMap.size();
        int rows = Math.max(1, (itemCount + 8) / 9);
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, "Dimensional Pocket");

        Material selected = modeManager.getPlacementMaterial(player.getUniqueId());

        for (int i = 0; i < size; i++) {
            if (!slotMaterialMap.containsKey(i)) continue;

            Material mat = slotMaterialMap.get(i);
            int count = plugin.getStorageManager().getAmount(player.getUniqueId(), mat.name());

            ItemStack icon = new ItemStack(mat, 1);
            ItemMeta meta = icon.getItemMeta();
            meta.setDisplayName(mat.name());
            List<String> lore = new ArrayList<>();
            lore.add("Stored: " + count);
            lore.add("§7Left-click: withdraw");
            lore.add("§7Shift-left (inv): deposit");
            lore.add("§7Right-click: toggle placement");
            if (selected != null && selected == mat) {
                lore.add("§aPlacement mode: SELECTED");
            }
            meta.setLore(lore);
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
        int rawSlot = event.getRawSlot();

        int topSize = event.getView().getTopInventory().getSize();

        // GUI Slots: Toggle placement mode (RIGHT-click)
        if (rawSlot >= 0 && rawSlot < topSize && slotMaterialMap.containsKey(rawSlot)
            && event.getClick().isRightClick()) {

            Material mat = slotMaterialMap.get(rawSlot);
            UUID uuid = player.getUniqueId();
            Material current = modeManager.getPlacementMaterial(uuid);

            if (current == mat) {
                plugin.disablePlacementMode(player);
                player.sendMessage("§aDimensional Pocket placement mode disabled.");
            } else {
                plugin.enablePlacementMode(player, mat);
                player.sendMessage("§aDimensional Pocket placement mode enabled: §f" + mat.name());
                player.sendMessage("§7Your pocket turns into the block. Hold right-click to place like vanilla.");
                player.sendMessage("§7Sneak-right-click to disable.");
            }

            openPocket(player);
            return;
        }

        // GUI Slots: Withdraw logic (LEFT-click)
        if (rawSlot >= 0 && rawSlot < topSize && event.getClick().isLeftClick() && slotMaterialMap.containsKey(rawSlot)) {
            Material mat = slotMaterialMap.get(rawSlot);
            UUID uuid = player.getUniqueId();
            int current = plugin.getStorageManager().getAmount(uuid, mat.name());

            if (current > 0 && player.getInventory().firstEmpty() != -1) {
                int withdrawAmount = Math.min(64, current);
                player.getInventory().addItem(new ItemStack(mat, withdrawAmount));
                plugin.getStorageManager().addAmount(uuid, mat.name(), -withdrawAmount);
                openPocket(player);
            } else {
                player.sendMessage("§cNot enough stored or inventory full.");
            }
        }

        // Player inventory: Deposit logic (SHIFT + LEFT-click)
        if (rawSlot >= event.getInventory().getSize() && event.getClick() == ClickType.SHIFT_LEFT) {

            ItemStack clicked = player.getInventory().getItem(event.getSlot());

            if (clicked != null && trackedMaterials.contains(clicked.getType())) {
                Material mat = clicked.getType();
                int amount = clicked.getAmount();

                plugin.getStorageManager().addAmount(player.getUniqueId(), mat.name(), amount);
                player.getInventory().clear(event.getSlot());
                openPocket(player);
            }
        }
    }
}
