package es.upv.inf.macammoc.autosave.AutoSave;

import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.FileConfigurationOptions;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoSave extends JavaPlugin {

    protected int taskID;
    protected boolean isRunning = false;

    @Override
    public void onEnable() {
        // Configuration: times
        this.getConfig().addDefault("Time.minutes.interval", 10);

        // Configuration: broadcast modes
        this.getConfig().addDefault("Broadcast.SilentMode", false);
        this.getConfig().addDefault("Broadcast.ServerBroadcast", true);
        this.getConfig().addDefault("Broadcast.ConsoleLogging", true);
        this.getConfig().addDefault("Broadcast.BroadcastEachWorld", false);
        this.getConfig().addDefault("Broadcast.AdminOnlyBroadcast", false);
        this.getConfig().addDefault("Broadcast.PreAlertMessage", true);

        // Configuration: Language
        this.getConfig().addDefault("Language.AutoSaveMessage", "- Saving worlds -");
        this.getConfig().addDefault("Language.PlayerMessage", "Saved...");
        this.getConfig().addDefault("Language.AdminMessage", "Saved...");
        this.getConfig().addDefault("Language.NoPermission", "You do not have permission");
        this.getConfig().addDefault("Language.AutosaveStarted", "Autosave Timer Started");
        this.getConfig().addDefault("Language.AutosaveAlreadyStarted", "Autosave Timer Already Started");
        this.getConfig().addDefault("Language.AutosaveHalted", "Autosave Timer Stopped");
        this.getConfig().addDefault("Language.AutosaveAlreadyHalted", "Autosave Timer Already Stopped");
        this.getConfig().addDefault("Language.Config Missing", "Config missing, generating a new one...");
        this.getConfig().addDefault("Language.ConfigReloaded", "AutoSave Config Reloaded");
        this.getConfig().addDefault("Language.AutosaveACTIVE", "AutoSave is ACTIVE");
        this.getConfig().addDefault("Language.AutosaveINACTIVE", "AutoSave is NOT ACTIVE");
        this.getConfig().addDefault("Language.help.toggle", "- Toggle AutoSave");
        this.getConfig().addDefault("Language.help.status", "- Check AutoSave status");
        this.getConfig().addDefault("Language.help.save", "- Force AutoSave");
        this.getConfig().addDefault("Language.help.reload", "- Reload default config");
        this.saveConfig();
        this.getConfig().options().copyDefaults(true);

        FileConfiguration cfg = this.getConfig();
        FileConfigurationOptions cfgOptions = cfg.options();
        cfgOptions.copyDefaults(true);
        cfgOptions.copyHeader(true);
        this.saveConfig();
        this.getCommand("asave").setExecutor((CommandExecutor) new Commands(this));
        this.StartAutoSave();
        this.getLogger().log(Level.INFO, "[AutoSave] {0} enabled.", this.getDescription().getVersion());

    }

    @Override
    public void onDisable() {
        this.getLogger().log(Level.INFO, "[AutoSave] {0} disabled.", this.getDescription().getVersion());
    }

    protected void displayHelp(CommandSender sender) {
        sender.sendMessage((Object) ChatColor.GOLD + "[ " + (Object) ChatColor.WHITE + "AutoSave " + this.getDescription().getVersion() + (Object) ChatColor.GOLD + " ]");
        sender.sendMessage((Object) ChatColor.YELLOW + " /asave on/off " + (Object) ChatColor.WHITE + this.getConfig().getString("Language.help.toggle"));
        sender.sendMessage((Object) ChatColor.YELLOW + " /asave status " + (Object) ChatColor.WHITE + this.getConfig().getString("Language.help.status"));
        sender.sendMessage((Object) ChatColor.YELLOW + " /asave save " + (Object) ChatColor.WHITE + this.getConfig().getString("Language.help.save"));
        if (sender.hasPermission("autosave.reload")) {
            sender.sendMessage((Object) ChatColor.YELLOW + " /asave reload " + (Object) ChatColor.WHITE + this.getConfig().getString("Language.help.reload"));
        }
    }

    protected boolean AutoSaveMap() {
        boolean silentMode = this.getConfig().getBoolean("Broadcast.SilentMode");
        boolean serverBroadcast = this.getConfig().getBoolean("Broadcast.ServerBroadcast");
        boolean adminOnlyBroadcast = this.getConfig().getBoolean("Broadcast.AdminOnlyBroadcast");
        boolean consoleLogging = this.getConfig().getBoolean("Broadcast.ConsoleLogging");
        boolean broadcastEachWorld = this.getConfig().getBoolean("Broadcast.BroadcastEachWorld");
        String autoSaveMessage = this.getConfig().getString("Language.AutoSaveMessage");

        if (silentMode) {
            Bukkit.savePlayers();
            Bukkit.getWorlds().forEach((world) -> {
                world.save();
            });
            return true;
        }
        if (consoleLogging && !serverBroadcast && !adminOnlyBroadcast) {
            Bukkit.getConsoleSender().sendMessage(this.replaceColorMacros(autoSaveMessage));
            Bukkit.savePlayers();
            for (World world : Bukkit.getWorlds()) {
                if (broadcastEachWorld) {
                    String worldName = world.getName();
                    this.getLogger().log(Level.INFO, "{0} Saved...", String.valueOf(worldName));
                }
                world.save();
            }
            return true;
        }
        if (serverBroadcast) {
            if (consoleLogging) {
                Bukkit.getConsoleSender().sendMessage(this.replaceColorMacros(autoSaveMessage));
            }
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.sendMessage(this.replaceColorMacros(autoSaveMessage));
            }
            Bukkit.savePlayers();
            for (World world : Bukkit.getWorlds()) {
                if (broadcastEachWorld) {
                    String worldName = world.getName();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        online.sendMessage(String.valueOf(worldName) + this.getConfig().getString("Language.PlayerMessage"));
                    }
                    if (consoleLogging) {
                        this.getLogger().log(Level.INFO, "{0} Saved...", String.valueOf(worldName));
                    }
                }
                world.save();
            }
            return true;
        }
        if (adminOnlyBroadcast) {
            if (consoleLogging) {
                Bukkit.getConsoleSender().sendMessage(this.replaceColorMacros(autoSaveMessage));
            }
            for (Player admin : Bukkit.getOnlinePlayers()) {
                if (!admin.hasPermission("autosave.admin")) {
                    continue;
                }
                admin.sendMessage(this.replaceColorMacros(autoSaveMessage));
            }
            Bukkit.savePlayers();
            for (World world : Bukkit.getWorlds()) {
                if (broadcastEachWorld) {
                    String worldName = world.getName();
                    for (Player admin : Bukkit.getOnlinePlayers()) {
                        if (!admin.hasPermission("autosave.admin")) {
                            continue;
                        }
                        admin.sendMessage(String.valueOf(worldName) + this.getConfig().getString("Language.AdminMessage"));
                    }
                    if (consoleLogging) {
                        this.getLogger().log(Level.INFO, "{0} Saved...", String.valueOf(worldName));
                    }
                }
                world.save();
            }
        }
        return true;
    }

    protected boolean StartAutoSave() {
        if (this.isRunning) {
            this.getLogger().info("AutoSave already running");
            return false;
        }
        int Interval = this.getConfig().getInt("Time.minutes.interval") * 1200;
        this.taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(Bukkit.getServer().getPluginManager().getPlugin("AutoSave"), () -> {
            AutoSave.this.AutoSaveMap();
        }, (long) Interval, (long) Interval);
        this.isRunning = true;
        this.getLogger().info("AutoSave started.");
        return true;
    }

    protected boolean StopAutoSave() {
        if (!this.isRunning) {
            this.getLogger().info("AutoSave is not running");
            return false;
        }
        Bukkit.getScheduler().cancelTask(this.taskID);
        this.getLogger().info("AutoSave stopped.");
        this.isRunning = false;
        return true;
    }

    protected void output(CommandSender sender, String phrase) {
        String prefix = (Object) ChatColor.GOLD + "* ";
        if ("NoPermission".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.RED + this.getConfig().getString("Language.NoPermission"));
        }
        if ("Autosave Started".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.GREEN + this.getConfig().getString("Language.AutosaveStarted"));
        }
        if ("Autosave Already Started".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.GREEN + this.getConfig().getString("Language.AutosaveAlreadyStarted"));
        }
        if ("Autosave Halted".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.RED + this.getConfig().getString("Language.AutosaveHalted"));
        }
        if ("Autosave Already Halted".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.RED + this.getConfig().getString("Language.AutosaveAlreadyHalted"));
        }
        if ("Config Missing".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.GOLD + this.getConfig().getString("Language.Missing"));
        }
        if ("Config Reloaded".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.GREEN + this.getConfig().getString("Language.ConfigReloaded"));
        }
        if ("Autosave ACTIVE".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.GREEN + this.getConfig().getString("Language.AutosaveACTIVE"));
        }
        if ("Autosave INACTIVE".equals(phrase)) {
            sender.sendMessage(String.valueOf(prefix) + (Object) ChatColor.RED + this.getConfig().getString("Language.AutosaveINACTIVE"));
        }
    }

    private String replaceColorMacros(String str) {
        str = str.replace("`r", ChatColor.RED.toString());
        str = str.replace("`R", ChatColor.DARK_RED.toString());
        str = str.replace("`y", ChatColor.YELLOW.toString());
        str = str.replace("`Y", ChatColor.GOLD.toString());
        str = str.replace("`g", ChatColor.GREEN.toString());
        str = str.replace("`G", ChatColor.DARK_GREEN.toString());
        str = str.replace("`c", ChatColor.AQUA.toString());
        str = str.replace("`C", ChatColor.DARK_AQUA.toString());
        str = str.replace("`b", ChatColor.BLUE.toString());
        str = str.replace("`B", ChatColor.DARK_BLUE.toString());
        str = str.replace("`p", ChatColor.LIGHT_PURPLE.toString());
        str = str.replace("`P", ChatColor.DARK_PURPLE.toString());
        str = str.replace("`0", ChatColor.BLACK.toString());
        str = str.replace("`1", ChatColor.DARK_GRAY.toString());
        str = str.replace("`2", ChatColor.GRAY.toString());
        str = str.replace("`w", ChatColor.WHITE.toString());
        return str;
    }

}
