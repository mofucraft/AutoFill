package command.common;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.List;

public abstract class CommandMethod {
    private final String argumentName;
    private final boolean adminCommand;

    public boolean isAdminCommand(){
        return this.adminCommand;
    }

    public CommandMethod(String argumentName, boolean adminCommand){
        this.argumentName = argumentName;
        this.adminCommand = adminCommand;
    }

    public String getArgumentName(){
        return argumentName;
    }

    public abstract boolean process(CommandSender sender, Command command, String label, String[] args);

    public abstract List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args);
}
