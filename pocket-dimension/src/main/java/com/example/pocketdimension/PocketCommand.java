package com.example.pocketdimension;

import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PocketCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Handle reload subcommand first (allow console)
        if (args.length > 0 && "reload".equalsIgnoreCase(args[0])) {
            // Allow console and server ops only
            if (!(sender instanceof org.bukkit.command.ConsoleCommandSender) && !sender.isOp()) {
                sender.sendMessage("§cYou don't have permission to do that.");
                return true;
            }

            sender.sendMessage("§eReloading pocket GUI...");
            boolean ok = PocketPlugin.getInstance().reloadGui();
            if (ok) {
                sender.sendMessage("§aPocket GUI reloaded.");
            } else {
                sender.sendMessage("§cFailed to reload Pocket GUI. Check console for details.");
            }
            return true;
        }

        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("pocketdimension.use")) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return true;
        }

        // Check for existing Dimensional Pocket
        for (ItemStack item : player.getInventory().getContents()) {
            if (PocketItem.isPocketItem(item)) {
                player.sendMessage("§eYou already have a Dimensional Pocket.");
                return true;
            }
        }

        // Give new pocket item
        ItemStack item = PocketItem.create();

        player.getInventory().addItem(item);
        player.sendMessage("§aYou have received a Dimensional Pocket.");
        return true;
    }
}
