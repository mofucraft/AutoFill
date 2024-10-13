package common;

import org.bukkit.plugin.java.JavaPlugin;

public class PluginUtil {
    private static JavaPlugin plugin = null;
    public static final String USING_COMMAND_NAME = "autofill";

    public static void setPlugin(JavaPlugin setPlugin){
        plugin = setPlugin;
    }
    public static JavaPlugin getPlugin(){
        return plugin;
    }
}
