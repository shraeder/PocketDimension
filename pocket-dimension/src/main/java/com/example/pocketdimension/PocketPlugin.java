package com.example.pocketdimension;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import com.example.pocketdimension.update.SpigetUpdateChecker;
import com.example.pocketdimension.update.UpdateCheckResult;
import com.example.pocketdimension.update.UpdateNotifyListener;
import com.example.pocketdimension.update.VersionComparator;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PocketPlugin extends JavaPlugin {
    private static PocketPlugin instance;
    private StorageManager storageManager;
    private PocketModeManager pocketModeManager;
    private PocketGhostItem pocketGhostItem;
    private PocketGUI pocketGUI;
    private PocketPickupListener pocketPickupListener;
    private PocketItemProtectionListener pocketItemProtectionListener;
    private PocketItemNormalizeListener pocketItemNormalizeListener;
    private PocketGhostItemListener pocketGhostItemListener;

    private volatile boolean updateAvailable;
    private volatile String latestAvailableVersion;
    private volatile String updateDownloadUrl;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.storageManager = new StorageManager(this);
        this.pocketModeManager = new PocketModeManager();
        this.pocketGhostItem = new PocketGhostItem(this);

        // Ensure default gui-items.yml is available in the plugin data folder
        saveResource("gui-items.yml", false);

        // Run migration and create/update snapshot
        try {
            runMigrationAndSnapshot();
        } catch (IOException e) {
            getLogger().severe("Failed during gui-items migration: " + e.getMessage());
        }

        // Create and register GUI listener
        this.pocketGUI = new PocketGUI(this);
        getServer().getPluginManager().registerEvents(this.pocketGUI, this);
        this.pocketPickupListener = new PocketPickupListener(this);
        getServer().getPluginManager().registerEvents(this.pocketPickupListener, this);
        this.pocketItemProtectionListener = new PocketItemProtectionListener();
        getServer().getPluginManager().registerEvents(this.pocketItemProtectionListener, this);

        this.pocketItemNormalizeListener = new PocketItemNormalizeListener(this);
        getServer().getPluginManager().registerEvents(this.pocketItemNormalizeListener, this);

        this.pocketGhostItemListener = new PocketGhostItemListener(this);
        getServer().getPluginManager().registerEvents(this.pocketGhostItemListener, this);

        getServer().getPluginManager().registerEvents(new UpdateNotifyListener(this), this);

        scheduleUpdateChecks();

        getCommand("pocket").setExecutor(new PocketCommand());
        getCommand("pocketleaderboard").setExecutor(new PocketLeaderboardCommand());
    }

    private void scheduleUpdateChecks() {
        if (!getConfig().getBoolean("update-check.enabled", true)) return;

        // Initial check shortly after startup
        getServer().getScheduler().runTaskAsynchronously(this, this::checkForUpdates);

        int hours = Math.max(1, getConfig().getInt("update-check.interval-hours", 24));
        long periodTicks = hours * 60L * 60L * 20L;
        getServer().getScheduler().runTaskTimerAsynchronously(this, this::checkForUpdates, periodTicks, periodTicks);
    }

    private void checkForUpdates() {
        try {
            String provider = getConfig().getString("update-check.provider", "spiget");
            if (!"spiget".equalsIgnoreCase(provider)) return;

            int resourceId = getConfig().getInt("update-check.spigot-resource-id", 124854);
            String url = getConfig().getString("update-check.download-url", "https://www.spigotmc.org/resources/pocket-dimension.124854/");

            SpigetUpdateChecker checker = new SpigetUpdateChecker(resourceId, url);
            UpdateCheckResult result = checker.fetchLatest();

            String current = getDescription().getVersion();
            String latest = result.getLatestVersion();

            this.updateDownloadUrl = result.getDownloadUrl();
            this.latestAvailableVersion = latest;
            this.updateAvailable = VersionComparator.isNewer(latest, current);

            if (this.updateAvailable) {
                getLogger().warning("A new PocketDimension version is available: " + current + " -> " + latest);
                getLogger().warning("Download: " + this.updateDownloadUrl);
            }
        } catch (Exception e) {
            getLogger().fine("Update check failed: " + e.getMessage());
        }
    }

    private void runMigrationAndSnapshot() throws IOException {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File current = new File(dataFolder, "gui-items.yml");
        File prev = new File(dataFolder, "gui-items-prev.yml");

        YamlConfiguration currentCfg = YamlConfiguration.loadConfiguration(current);

        if (!prev.exists()) {
            // First run: create snapshot for future comparisons
            currentCfg.save(prev);
            return;
        }

        YamlConfiguration prevCfg = YamlConfiguration.loadConfiguration(prev);

        List<String> prevItems = prevCfg.getStringList("items");
        List<String> currentItems = currentCfg.getStringList("items");

        int min = Math.min(prevItems.size(), currentItems.size());
        for (int i = 0; i < min; i++) {
            String p = prevItems.get(i);
            String c = currentItems.get(i);
            if (p == null || c == null) continue;
            String pmatName = p.toString();
            String cmatName = c.toString();
            if (!pmatName.equals(cmatName)) {
                getLogger().info("Migrating storage: " + pmatName + " -> " + cmatName);
                storageManager.migrateMaterial(pmatName, cmatName);
            }
        }

        // update snapshot
        currentCfg.save(prev);
    }

    public boolean reloadGui() {
        try {
            runMigrationAndSnapshot();

            if (this.pocketGUI != null) {
                HandlerList.unregisterAll(this.pocketGUI);
            }

            if (this.pocketPickupListener != null) {
                HandlerList.unregisterAll(this.pocketPickupListener);
            }

            if (this.pocketItemProtectionListener != null) {
                HandlerList.unregisterAll(this.pocketItemProtectionListener);
            }

            if (this.pocketItemNormalizeListener != null) {
                HandlerList.unregisterAll(this.pocketItemNormalizeListener);
            }

            if (this.pocketGhostItemListener != null) {
                HandlerList.unregisterAll(this.pocketGhostItemListener);
            }

            this.pocketGUI = new PocketGUI(this);
            getServer().getPluginManager().registerEvents(this.pocketGUI, this);

            this.pocketPickupListener = new PocketPickupListener(this);
            getServer().getPluginManager().registerEvents(this.pocketPickupListener, this);

            this.pocketItemProtectionListener = new PocketItemProtectionListener();
            getServer().getPluginManager().registerEvents(this.pocketItemProtectionListener, this);

            this.pocketItemNormalizeListener = new PocketItemNormalizeListener(this);
            getServer().getPluginManager().registerEvents(this.pocketItemNormalizeListener, this);

            this.pocketGhostItemListener = new PocketGhostItemListener(this);
            getServer().getPluginManager().registerEvents(this.pocketGhostItemListener, this);
            return true;
        } catch (IOException e) {
            getLogger().severe("Failed to reload GUI: " + e.getMessage());
            return false;
        }
    }

    public static PocketPlugin getInstance() {
        return instance;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public PocketModeManager getPocketModeManager() {
        return pocketModeManager;
    }

    public PocketGhostItem getPocketGhostItem() {
        return pocketGhostItem;
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestAvailableVersion() {
        return latestAvailableVersion;
    }

    public String getUpdateDownloadUrl() {
        String url = updateDownloadUrl;
        if (url == null || url.trim().isEmpty()) {
            return "https://www.spigotmc.org/resources/pocket-dimension.124854/";
        }
        return url;
    }

    public void enablePlacementMode(org.bukkit.entity.Player player, org.bukkit.Material material) {
        if (player == null || material == null) return;
        if (!material.isBlock()) return;

        java.util.UUID playerId = player.getUniqueId();
        pocketModeManager.setPlacementMaterial(playerId, material);

        // Only swap if they're holding the pocket item.
        org.bukkit.inventory.ItemStack inHand = player.getInventory().getItemInMainHand();
        if (!PocketItem.isPocketItem(inHand)) return;

        refreshGhostItem(player);
    }

    public void disablePlacementMode(org.bukkit.entity.Player player) {
        if (player == null) return;
        java.util.UUID playerId = player.getUniqueId();
        pocketModeManager.clearPlacementMode(playerId);

        org.bukkit.inventory.ItemStack inHand = player.getInventory().getItemInMainHand();
        if (pocketGhostItem.isGhost(inHand)) {
            player.getInventory().setItemInMainHand(PocketItem.create());
        }
    }

    public void refreshGhostItem(org.bukkit.entity.Player player) {
        if (player == null) return;
        java.util.UUID playerId = player.getUniqueId();
        org.bukkit.Material selected = pocketModeManager.getPlacementMaterial(playerId);
        if (selected == null) return;

        org.bukkit.inventory.ItemStack inHand = player.getInventory().getItemInMainHand();
        boolean handEmpty = inHand == null || inHand.getType() == org.bukkit.Material.AIR;
        boolean handIsPocketOrGhost = PocketItem.isPocketItem(inHand) || pocketGhostItem.isGhost(inHand);
        if (!handEmpty && !handIsPocketOrGhost) {
            // Don't overwrite tools/other items.
            return;
        }

        int stored = storageManager.getAmount(playerId, selected.name());
        if (stored <= 0) {
            disablePlacementMode(player);
            return;
        }

        org.bukkit.inventory.ItemStack ghost = pocketGhostItem.create(selected, 1, stored);
        player.getInventory().setItemInMainHand(ghost);
    }

    public void updateGhostItemIfPresent(org.bukkit.entity.Player player) {
        if (player == null) return;

        java.util.UUID playerId = player.getUniqueId();
        org.bukkit.Material selected = pocketModeManager.getPlacementMaterial(playerId);
        if (selected == null) return;

        int stored = storageManager.getAmount(playerId, selected.name());

        boolean foundAny = false;
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();

        org.bukkit.inventory.ItemStack[] contents = inv.getContents();
        for (int i = 0; i < contents.length; i++) {
            org.bukkit.inventory.ItemStack stack = contents[i];
            if (!pocketGhostItem.isGhost(stack)) continue;

            foundAny = true;

            if (stored <= 0) {
                // Replace the ghost item back into a real pocket item and clear mode.
                contents[i] = PocketItem.create();
            } else {
                contents[i] = pocketGhostItem.create(selected, 1, stored);
            }
        }

        if (!foundAny) return;

        inv.setContents(contents);

        if (stored <= 0) {
            pocketModeManager.clearPlacementMode(playerId);
        }
    }
}
