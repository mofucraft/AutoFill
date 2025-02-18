package command.common;

import command.argument.Start;
import config.Config;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.management.InstanceNotFoundException;
import java.sql.SQLException;
import java.util.*;

public class MainCommand extends CommandMethod {
    public MainCommand(){
        super("",false,false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        try {
            Player p = (Player)sender;
            try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
                CommandListener.getCommandMethod(Start.class).process(sender, command, label, new String[]{"", String.valueOf(database.getPlayerStatus(p).getUseThread())});
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
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
