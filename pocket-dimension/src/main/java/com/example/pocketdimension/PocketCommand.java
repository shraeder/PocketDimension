package com.example.pocketdimension;

import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;

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
            if (item != null &&
                item.getType() == Material.GLASS_BOTTLE &&
                item.hasItemMeta() &&
                "§bDimensional Pocket".equals(item.getItemMeta().getDisplayName())) {
                player.sendMessage("§eYou already have a Dimensional Pocket.");
                return true;
            }
        }

        // Give new pocket item
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bDimensional Pocket");
        meta.setLore(Collections.singletonList("§7Shift-left-click while holding to open"));
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
        player.sendMessage("§aYou have received a Dimensional Pocket.");
        return true;
    }
}
