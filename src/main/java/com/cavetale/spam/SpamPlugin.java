package com.cavetale.spam;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import org.json.simple.JSONValue;
import org.bukkit.entity.Player;
import org.bukkit.ChatColor;
import java.util.Map;
import java.util.HashMap;

public final class SpamPlugin extends JavaPlugin {
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
        }.runTaskTimer(this, 0, 20*60*10);
    }

    void spam() {
        reloadConfig();
        List<String> keys = new ArrayList<>(getConfig().getKeys(false));
        if (keys.isEmpty()) return;
        String key = keys.get(random.nextInt(keys.size()));
        getLogger().info("Announcing " + key + "...");
        ConfigurationSection conf = getConfig().getConfigurationSection(key);
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
}
