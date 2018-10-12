package com.gmail.andrewriachi.FundMyServer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Logger;

public class FundMyServer extends JavaPlugin {
    private static File configFile;
    public static YamlConfiguration config;
    public static String url;
    public static Logger log;
    public static File pluginFolder;

    public void onEnable() {
        log = getLogger();
        pluginFolder = getDataFolder();
        pluginFolder.mkdir();
        configFile = new File(pluginFolder, "config.yml");
        config = (YamlConfiguration) getConfig();
        try {
            config.save(configFile);
        } catch (Exception ex) {
            log.warning("Could not save config");
        }

        url = config.getString("url");
        getServer().getPluginManager().registerEvents(new JoinListener(), this);
        this.getCommand("donationadd").setExecutor(new DonationAdd());
    }

    public void onDisable() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    public static void saveFile(FileConfiguration configuration, File file) {
        try {
            configuration.save(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void crash(String msg) {
        log.severe(msg);
        log.severe("Disabling...");
        Bukkit.getPluginManager().disablePlugin(JavaPlugin.getPlugin(FundMyServer.class));
    }

}
