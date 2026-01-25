package com.example.pocketdimension;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadGuiCommand implements CommandExecutor {
    private final PocketPlugin plugin;

    public ReloadGuiCommand(PocketPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("pocketdimension.reload")) {
            sender.sendMessage("§cYou don't have permission to do that.");
            return true;
        }

        sender.sendMessage("§eReloading pocket GUI...");
        boolean ok = plugin.reloadGui();
        if (ok) {
            sender.sendMessage("§aPocket GUI reloaded.");
        } else {
            sender.sendMessage("§cFailed to reload Pocket GUI. Check console for details.");
        }
        return true;
    }
}
