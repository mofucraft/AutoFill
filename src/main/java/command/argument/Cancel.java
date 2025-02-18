package command.argument;

import command.common.CommandMethod;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.UserData;

import java.sql.SQLException;
import java.util.*;

public class Cancel extends CommandMethod {
    public Cancel(){
        super("cancel",false,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        Integer count = null;
        if(args.length >= 2) {
            if(NumberUtils.isNumber(args[1])) {
                count = Integer.parseInt(args[1]) - 1;
            }
            else {
                try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                    LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "numberParseFailed");
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                return false;
            }
        }
        userData.stopFillTask(count);
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
