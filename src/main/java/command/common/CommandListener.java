package command.common;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class CommandListener implements CommandExecutor {
    private static ArrayList<String> subCommandNames;
    private final TabCompleterListener tabCompleterListener;
    private final ArrayList<CommandMethod> commandMethods;
    private final CommandMethod baseCommand;

    public static ArrayList<String> getSubCommandNames(){
        return subCommandNames;
    }
    public ArrayList<CommandMethod> getCommandMethods() { return this.commandMethods; }

    public CommandListener(TabCompleterListener tabCompleterListener){
        this.commandMethods = tabCompleterListener.getCommandMethods();
        this.tabCompleterListener = tabCompleterListener;
        subCommandNames = tabCompleterListener.getSubCommandNames();
        this.baseCommand = new Command();
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if(args.length < 1){
            return baseCommand.process(sender,command,label,args);
        }
        for(CommandMethod commandMethod : commandMethods){
            if(commandMethod.getArgumentName().equalsIgnoreCase(args[0])) {
                return commandMethod.process(sender,command,label,args);
            }
        }
        return true;
    }
}
