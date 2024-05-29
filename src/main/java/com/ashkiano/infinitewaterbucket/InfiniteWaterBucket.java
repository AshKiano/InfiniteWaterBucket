package com.ashkiano.infinitewaterbucket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class InfiniteWaterBucket extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        this.getCommand("infinitewater").setExecutor(new BucketCommand());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
        if (!getConfig().isSet("permission")) {
            getConfig().set("permission", "infinitewater.use");
            saveConfig();
        }
        Metrics metrics = new Metrics(this, 19473);
        this.getLogger().info("Thank you for using the InfiniteWaterBucket plugin! If you enjoy using this plugin, please consider making a donation to support the development. You can donate at: https://donate.ashkiano.com");
        checkForUpdates();
    }

    public class BucketCommand implements CommandExecutor {
        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if (sender instanceof Player) {
                Player player = (Player) sender;

                String permission = InfiniteWaterBucket.this.getConfig().getString("permission");

                if (!player.hasPermission(permission)) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }

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

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        if (droppedItem.getType() == Material.BUCKET && droppedItem.hasItemMeta() && droppedItem.getItemMeta().hasLore()) {
            List<String> lore = droppedItem.getItemMeta().getLore();
            if (lore != null && lore.contains("Infinite Bucket")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You cannot drop the infinite water bucket!");
            }
        }
    }

    private void checkForUpdates() {
        try {
            String pluginName = this.getDescription().getName();
            URL url = new URL("https://www.ashkiano.com/version_check.php?plugin=" + pluginName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");

            int responseCode = con.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String jsonResponse = response.toString();
                JSONObject jsonObject = new JSONObject(jsonResponse);
                if (jsonObject.has("error")) {
                    this.getLogger().warning("Error when checking for updates: " + jsonObject.getString("error"));
                } else {
                    String latestVersion = jsonObject.getString("latest_version");

                    String currentVersion = this.getDescription().getVersion();
                    if (currentVersion.equals(latestVersion)) {
                        this.getLogger().info("This plugin is up to date!");
                    } else {
                        this.getLogger().warning("There is a newer version (" + latestVersion + ") available! Please update!");
                    }
                }
            } else {
                this.getLogger().warning("Failed to check for updates. Response code: " + responseCode);
            }
        } catch (Exception e) {
            this.getLogger().warning("Failed to check for updates. Error: " + e.getMessage());
        }
    }
}
