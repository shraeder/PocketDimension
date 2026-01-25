package com.example.pocketdimension;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class StorageManager {
    private final File storageFile;
    private final YamlConfiguration config;

    public StorageManager(JavaPlugin plugin) {
        storageFile = new File(plugin.getDataFolder(), "storage.yml");
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        config = YamlConfiguration.loadConfiguration(storageFile);
    }

    public int getAmount(UUID uuid, String material) {
        return config.getInt(uuid + "." + material, 0);
    }

    public void setAmount(UUID uuid, String material, int amount) {
        config.set(uuid + "." + material, Math.max(0, amount));
        save();
    }

    public void addAmount(UUID uuid, String material, int amount) {
        setAmount(uuid, material, getAmount(uuid, material) + amount);
    }

    public void migrateMaterial(String oldMaterial, String newMaterial) {
        if (oldMaterial == null || newMaterial == null) return;
        if (oldMaterial.equals(newMaterial)) return;

        for (String key : config.getKeys(false)) {
            // top-level keys are UUID strings
            String oldPath = key + "." + oldMaterial;
            int oldVal = config.getInt(oldPath, 0);
            if (oldVal > 0) {
                String newPath = key + "." + newMaterial;
                int newVal = config.getInt(newPath, 0);
                config.set(newPath, newVal + oldVal);
                config.set(oldPath, null);
            }
        }
        save();
    }

    public void save() {
        try {
            config.save(storageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
