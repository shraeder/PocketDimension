package com.example.pocketdimension;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class PocketLeaderboardCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pocketdimension.leaderboard")) {
            sender.sendMessage("§cYou do not have permission to view the leaderboard.");
            return true;
        }

        File file = new File(PocketPlugin.getInstance().getDataFolder(), "storage.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<UUID, Integer> totalsByPlayer = new HashMap<>();
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException ignored) {
                continue;
            }

            ConfigurationSection section = config.getConfigurationSection(uuidStr);
            if (section == null) continue;

            int total = 0;
            for (String key : section.getKeys(false)) {
                // Each key is expected to be a material name with an integer amount.
                int amt = section.getInt(key, 0);
                if (amt > 0) total += amt;
            }

            if (total > 0) {
                totalsByPlayer.put(uuid, total);
            }
        }

        if (totalsByPlayer.isEmpty()) {
            sender.sendMessage("§6§l--- Pocket Leaderboard ---");
            sender.sendMessage("§7No stored items yet.");
            return true;
        }

        List<Map.Entry<UUID, Integer>> sorted = new ArrayList<>(totalsByPlayer.entrySet());
        sorted.sort((a, b) -> Integer.compare(b.getValue(), a.getValue()));

        int limit = 10;
        sender.sendMessage("§6§l--- Pocket Leaderboard (Total Stored) ---");
        for (int i = 0; i < Math.min(limit, sorted.size()); i++) {
            Map.Entry<UUID, Integer> entry = sorted.get(i);
            UUID uuid = entry.getKey();
            int total = entry.getValue();

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            String name = player.getName() != null ? player.getName() : uuid.toString();
            sender.sendMessage("§e#" + (i + 1) + " §a" + name + " §7- §b" + total);
        }

        return true;
    }
}
