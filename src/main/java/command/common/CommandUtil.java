package command.common;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public class CommandUtil {
    public static void addCommandAndTabCompleter(JavaPlugin plugin, String commandName, ArrayList<CommandMethod> commandMethods){
        TabCompleterListener tabCompleterListener = new TabCompleterListener(commandMethods);
        plugin.getCommand(commandName).setExecutor(new CommandListener(tabCompleterListener));
        plugin.getCommand(commandName).setTabCompleter(tabCompleterListener);
    }
}
