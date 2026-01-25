package com.example.pocketdimension;

import org.bukkit.Material;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PocketModeManager {
    private final Map<UUID, Material> placementMaterialByPlayer = new ConcurrentHashMap<>();

    public Material getPlacementMaterial(UUID playerId) {
        return placementMaterialByPlayer.get(playerId);
    }

    public boolean isPlacementModeEnabled(UUID playerId) {
        return placementMaterialByPlayer.containsKey(playerId);
    }

    public void setPlacementMaterial(UUID playerId, Material material) {
        if (playerId == null || material == null) return;
        placementMaterialByPlayer.put(playerId, material);
    }

    public void clearPlacementMode(UUID playerId) {
        if (playerId == null) return;
        placementMaterialByPlayer.remove(playerId);
    }
}
