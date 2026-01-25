package com.example.pocketdimension;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PocketPlugin extends JavaPlugin {
    private static PocketPlugin instance;
    private StorageManager storageManager;
    private PocketGUI pocketGUI;
    private PocketPickupListener pocketPickupListener;
    private PocketItemProtectionListener pocketItemProtectionListener;
    private PocketItemNormalizeListener pocketItemNormalizeListener;

    @Override
    public void onEnable() {
        instance = this;
        this.storageManager = new StorageManager(this);

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

        getCommand("pocket").setExecutor(new PocketCommand());
        getCommand("pocketleaderboard").setExecutor(new PocketLeaderboardCommand());
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

            this.pocketGUI = new PocketGUI(this);
            getServer().getPluginManager().registerEvents(this.pocketGUI, this);

            this.pocketPickupListener = new PocketPickupListener(this);
            getServer().getPluginManager().registerEvents(this.pocketPickupListener, this);

            this.pocketItemProtectionListener = new PocketItemProtectionListener();
            getServer().getPluginManager().registerEvents(this.pocketItemProtectionListener, this);

            this.pocketItemNormalizeListener = new PocketItemNormalizeListener(this);
            getServer().getPluginManager().registerEvents(this.pocketItemNormalizeListener, this);
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
}
