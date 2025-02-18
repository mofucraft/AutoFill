package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.UserData;
import org.minecraft.autofill.SelectMode;

import java.sql.SQLException;
import java.util.*;

public class CopySet extends CommandMethod {
    public CopySet(){
        super("copyset",false,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        userData.setSelectMode(SelectMode.COPY);
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            Map<String, String> variables = new HashMap<>();
            variables.put("wand", Config.getWand().toString());
            LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"copyPositionSetting", variables);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
