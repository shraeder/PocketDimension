package com.example.pocketdimension.update;

import com.example.pocketdimension.PocketPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class UpdateNotifyListener implements Listener {
    private final PocketPlugin plugin;

    public UpdateNotifyListener(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!plugin.isUpdateAvailable()) return;
        if (!event.getPlayer().isOp()) return;
        if (!plugin.getConfig().getBoolean("update-check.notify-ops-on-join", true)) return;

        String current = plugin.getDescription().getVersion();
        String latest = plugin.getLatestAvailableVersion();
        String url = plugin.getUpdateDownloadUrl();

        event.getPlayer().sendMessage("§ePocketDimension update available: §f" + current + " §e→ §a" + latest);
        event.getPlayer().sendMessage("§7Download: §b" + url);
    }
}
