package command.common;

import common.Util;
import config.Config;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public class TabCompleterListener implements TabCompleter {
    private final ArrayList<String> argumentNames;
    private final ArrayList<String> adminArgumentNames;

    private final ArrayList<CommandMethod> commandMethods;

    protected ArrayList<CommandMethod> getCommandMethods() { return this.commandMethods; }

    public TabCompleterListener(ArrayList<CommandMethod> commandMethods){
        this.commandMethods = commandMethods;
        this.argumentNames = new ArrayList<>();
        this.adminArgumentNames = new ArrayList<>();
        for(CommandMethod commandMethod : this.commandMethods){
            this.adminArgumentNames.add(commandMethod.getArgumentName());
            if(!commandMethod.isAdminCommand()) {
                this.argumentNames.add(commandMethod.getArgumentName());
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        boolean hasAdminPermission = Util.hasValidPermission((Player)commandSender, Config.getAdminPermission());
        if(args[0] == null || args[0].isEmpty()) {
            completions = this.getArgumentNames(hasAdminPermission);
        }
        else if(this.getArgumentNames(hasAdminPermission).contains(args[0].toLowerCase())){
            for(CommandMethod commandMethod : commandMethods){
                if(commandMethod.getArgumentName().equalsIgnoreCase(args[0])) {
                    completions = commandMethod.tabCompleterProcess(commandSender,command,label,args);
                }
            }
        }
        else {
            StringUtil.copyPartialMatches(args[0], this.getArgumentNames(hasAdminPermission), completions);
        }
        return completions;
    }

    public ArrayList<String> getArgumentNames(boolean hasAdminPermission){
        if(hasAdminPermission) {
            return this.adminArgumentNames;
        }
        else{
            return this.argumentNames;
        }
    }
}
