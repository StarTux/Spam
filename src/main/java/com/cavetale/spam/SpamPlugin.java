package com.cavetale.spam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONValue;

public final class SpamPlugin extends JavaPlugin implements Listener {
    Random random = new Random(System.currentTimeMillis());

    @Override
    public void onEnable() {
        reloadConfig();
        saveDefaultConfig();
        new BukkitRunnable() {
            @Override
            public void run() {
                spam();
            }
        }.runTaskTimer(this, 20*60*5, 20*60*10);
        for (Player player: getServer().getOnlinePlayers()) {
            setupPlayerList(player);
        }
        getServer().getPluginManager().registerEvents(this, this);
    }

    void spam() {
        reloadConfig();
        ConfigurationSection section = getConfig().getConfigurationSection("messages");
        List<String> keys = new ArrayList<>(section.getKeys(false));
        if (keys.isEmpty()) return;
        String key = keys.get(random.nextInt(keys.size()));
        getLogger().info("Announcing " + key + "...");
        ConfigurationSection conf = section.getConfigurationSection(key);
        String msg = ChatColor.translateAlternateColorCodes('&', conf.getString("Message"));
        String cmd = conf.getString("Command");
        String url = conf.getString("URL");
        String color = conf.getString("Color");
        Map<String, Object> json = new HashMap<>();
        json.put("text", msg);
        if (cmd != null) {
            Map<String, Object> clickEvent = new HashMap<>();
            if (cmd.endsWith(" ")) {
                clickEvent.put("action", "suggest_command");
            } else {
                clickEvent.put("action", "run_command");
            }
            clickEvent.put("value", cmd);
            json.put("clickEvent", clickEvent);
        }
        if (url != null) {
            Map<String, Object> clickEvent = new HashMap<>();
            clickEvent.put("action", "open_url");
            clickEvent.put("value", url);
            json.put("clickEvent", clickEvent);
        }
        if (color != null) {
            json.put("color", color);
        }
        String c = "minecraft:tellraw %s " + JSONValue.toJSONString(json);
        for (Player player: getServer().getOnlinePlayers()) {
            getServer().dispatchCommand(getServer().getConsoleSender(), String.format(c, player.getName()));
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        setupPlayerList(event.getPlayer());
    }

    void setupPlayerList(Player player) {
        String header = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Header"));
        String footer = ChatColor.translateAlternateColorCodes('&', getConfig().getString("Footer"));
        player.setPlayerListHeader(header);
        player.setPlayerListFooter(footer);
    }
}
