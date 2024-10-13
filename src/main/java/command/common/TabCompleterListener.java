package command.common;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterListener implements TabCompleter {
    private final ArrayList<String> subCommandNames;

    private final ArrayList<CommandMethod> commandMethods;

    public ArrayList<String> getSubCommandNames(){
        return this.subCommandNames;
    }

    public ArrayList<CommandMethod> getCommandMethods() { return this.commandMethods; }


    public TabCompleterListener(ArrayList<CommandMethod> commandMethods){
        this.commandMethods = commandMethods;
        this.subCommandNames = new ArrayList<>();
        for(CommandMethod commandMethod : this.commandMethods){
            this.subCommandNames.add(commandMethod.getArgumentName());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args[0] == null || args[0].isEmpty()) {
            completions = CommandListener.getSubCommandNames();
        }
        else if(CommandListener.getSubCommandNames().contains(args[0].toLowerCase())){
            for(CommandMethod commandMethod : commandMethods){
                if(commandMethod.getArgumentName().equalsIgnoreCase(args[0])) {
                    completions = commandMethod.tabCompleterProcess(commandSender,command,label,args);
                }
            }
        }
        else {
            StringUtil.copyPartialMatches(args[0], CommandListener.getSubCommandNames(), completions);
        }
        return completions;
    }
}
