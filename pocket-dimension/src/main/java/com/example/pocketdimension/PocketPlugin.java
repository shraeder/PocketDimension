package com.example.pocketdimension;

import org.bukkit.plugin.java.JavaPlugin;

public class PocketPlugin extends JavaPlugin {
    private static PocketPlugin instance;
    private StorageManager storageManager;

    @Override
        public void onEnable() {
            instance = this;
            this.storageManager = new StorageManager(this);

            getServer().getPluginManager().registerEvents(new PocketGUI(this), this);
            getServer().getPluginManager().registerEvents(new PocketPickupListener(this), this);

            getCommand("pocket").setExecutor(new PocketCommand());
            getCommand("pocketleaderboard").setExecutor(new PocketLeaderboardCommand());
        }

    public static PocketPlugin getInstance() {
        return instance;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }
}
