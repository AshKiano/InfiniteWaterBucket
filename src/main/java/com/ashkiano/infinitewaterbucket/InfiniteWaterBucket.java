package com.ashkiano.infinitewaterbucket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.ArrayList;
import java.util.List;

//TODO pridat permisi
public class InfiniteWaterBucket extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.getCommand("infinitewater").setExecutor(new BucketCommand());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        Metrics metrics = new Metrics(this, 19473);
        this.getLogger().info("Thank you for using the InfiniteWaterBucket plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://paypal.me/josefvyskocil");
    }

    public class BucketCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                ItemStack bucket = new ItemStack(Material.WATER_BUCKET);
                ItemMeta meta = bucket.getItemMeta();

                List<String> lore = new ArrayList<>();
                lore.add("Infinite Bucket");

                if (meta != null) {
                    meta.setLore(lore);
                    bucket.setItemMeta(meta);
                }

                player.getInventory().addItem(bucket);
                player.sendMessage("You've received an infinite water bucket!");
            }

            return true;
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (handItem.hasItemMeta() && handItem.getItemMeta().hasLore()) {
            List<String> lore = handItem.getItemMeta().getLore();
            if (lore != null && lore.contains("Infinite Bucket")) {

                event.getBlockClicked().getRelative(event.getBlockFace()).setType(Material.WATER);

                Bukkit.getScheduler().runTaskLater(this, () -> {
                    ItemStack infiniteBucket = new ItemStack(Material.WATER_BUCKET);
                    ItemMeta meta = infiniteBucket.getItemMeta();

                    if (meta != null) {
                        List<String> newLore = new ArrayList<>();
                        newLore.add("Infinite Bucket");
                        meta.setLore(newLore);
                        infiniteBucket.setItemMeta(meta);
                    }

                    player.getInventory().setItemInMainHand(infiniteBucket);
                }, 1L);
            }
        }
    }
}