package common;

import api.CoreProtectAPI;
import api.EconomyAPI;
import api.JobsAPI;
import api.WorldGuardAPI;
import command.argument.*;
import command.common.CommandUtil;
import command.argument.basecommand.Language;
import command.argument.basecommand.Reload;
import config.Config;
import database.common.DatabaseUtil;
import event.PluginEventHandler;
import language.LanguageUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {
    private static boolean isInitialized = false;

    public static void initializePlugin(JavaPlugin plugin){
        if(isInitialized) finalizePlugin(plugin);
        PluginUtil.setPlugin(plugin);
        Bukkit.getPluginManager().registerEvents(new PluginEventHandler(), plugin);
        CommandUtil.addCommandAndTabCompleter(plugin,PluginUtil.USING_COMMAND_NAME,new ArrayList<>(Arrays.asList(
                new Reload(),
                new Language(),
                new CopySet(),
                new GetList(),
                new Mode(),
                new Rotation(),
                new Cancel())));
        reloadPlugin(plugin);
        isInitialized = true;
    }

    public static void reloadPlugin(JavaPlugin plugin){
        plugin.saveDefaultConfig();
        plugin.saveResource("language/ja.lang.yml",false);
        plugin.saveResource("language/.lang.ymlの前の名前は2文字にしてください", false);
        DatabaseUtil.initialize(plugin,"database.db");
        LanguageUtil.refreshLanguage(plugin);
        try {
            Config.refreshConfig(plugin);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        CoreProtectAPI.initialize(plugin);
        EconomyAPI.initialize(plugin);
        JobsAPI.initialize(plugin);
        WorldGuardAPI.initialize(plugin);
    }

    public static void finalizePlugin(JavaPlugin plugin){
        CoreProtectAPI.finalize(plugin);
        EconomyAPI.finalize(plugin);
        JobsAPI.finalize(plugin);
        WorldGuardAPI.finalize(plugin);
    }
}
