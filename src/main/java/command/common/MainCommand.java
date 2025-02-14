package command.common;

import command.argument.Start;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import javax.management.InstanceNotFoundException;
import java.util.*;

public class MainCommand extends CommandMethod {
    public MainCommand(){
        super("",false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        try {
            CommandListener.getCommandMethod(Start.class).process(sender, command, label, new String[]{"","1"});
        } catch (InstanceNotFoundException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
