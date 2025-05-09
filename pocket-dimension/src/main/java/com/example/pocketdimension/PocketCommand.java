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
        if (!(sender instanceof Player)) return false;

        Player player = (Player) sender;
        ItemStack item = new ItemStack(Material.GLASS_BOTTLE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bDimensional Pocket");
        meta.setLore(Collections.singletonList("§7Shift-left-click while holding to open"));
        item.setItemMeta(meta);

        player.getInventory().addItem(item);
        player.sendMessage("§aYou have been given a Dimensional Pocket.");
        return true;
    }
}
