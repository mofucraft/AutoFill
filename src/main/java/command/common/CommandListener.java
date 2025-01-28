package command.common;

import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import javax.management.InstanceNotFoundException;
import java.util.ArrayList;

public class CommandListener implements CommandExecutor {
    private static ArrayList<CommandMethod> commandMethods;
    private static CommandMethod baseCommand;

    public static <T> T getCommandMethod(Class<T> commandMethodClass) throws InstanceNotFoundException {
        if(commandMethodClass.isInstance(baseCommand)) return (T) baseCommand;
        for(CommandMethod commandMethod: commandMethods){
            if(commandMethodClass.isInstance(commandMethod)){
                return (T) commandMethod;
            }
        }
        throw new InstanceNotFoundException();
    }

    public CommandListener(TabCompleterListener tabCompleterListener){
        commandMethods = tabCompleterListener.getCommandMethods();
        baseCommand = new MainCommand();
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
