package com.example.pocketdimension;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PocketLeaderboardCommand implements CommandExecutor {
    private final List<Material> trackedMaterials = Arrays.asList(
        Material.COBBLESTONE, Material.COBBLED_DEEPSLATE, Material.DIORITE,
        Material.ANDESITE, Material.GRANITE, Material.GRAVEL, Material.DIRT,
        Material.SAND, Material.NETHERRACK
    );

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pocketdimension.leaderboard")) {
            sender.sendMessage("§cYou do not have permission to view the leaderboard.");
            return true;
        }

        File file = new File(PocketPlugin.getInstance().getDataFolder(), "storage.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<Material, String> topPlayers = new HashMap<>();
        Map<Material, Integer> topAmounts = new HashMap<>();

        for (Material mat : trackedMaterials) {
            int highest = 0;
            String topName = "None";

            for (String uuidStr : config.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                int amount = config.getInt(uuidStr + "." + mat.name(), 0);
                if (amount > highest) {
                    highest = amount;
                    OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
                    topName = player.getName() != null ? player.getName() : uuid.toString();
                }
            }

            topPlayers.put(mat, topName);
            topAmounts.put(mat, highest);
        }

        sender.sendMessage("§6§l--- Pocket Leaderboard ---");
        for (Material mat : trackedMaterials) {
            String name = topPlayers.get(mat);
            int amt = topAmounts.get(mat);
            sender.sendMessage("§e" + mat.name() + ": §a" + name + " §7(" + amt + ")");
        }

        return true;
    }
}
