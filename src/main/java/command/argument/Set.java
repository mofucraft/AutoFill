package command.argument;

import command.common.CommandMethod;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.apache.commons.lang.math.NumberUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.*;

public class Set extends CommandMethod {
    public Set(){
        super("set",true,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            if(args.length < 4){
                LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "notEnoughArgument");
                return false;
            }
            if("maxThread".equalsIgnoreCase(args[2])){
                PlayerStatus changePlayerStatus = database.getPlayerStatusByName(args[1]);
                if(changePlayerStatus == null){
                    LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "userDataNotFound");
                    return false;
                }
                if(NumberUtils.isNumber(args[3])) {
                    int oldThreadCount = changePlayerStatus.getMaxThread();
                    changePlayerStatus.setMaxThread(Integer.parseInt(args[3]));
                    database.updatePlayerStatus(changePlayerStatus);
                    Map<String, String> variables = new HashMap<>();
                    variables.put("name", changePlayerStatus.getName());
                    variables.put("variableName","MaxThread");
                    variables.put("oldValue",Integer.toString(oldThreadCount));
                    variables.put("value",Integer.toString(changePlayerStatus.getMaxThread()));
                    LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"updateUserData", variables);
                    return true;
                }
                else {
                    LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "numberParseFailed");
                    return false;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length >= 2){
            if(args.length < 3 || args[1] == null || args[1].isEmpty()){
                List<String> playerName = new ArrayList<>();
                for (Player p : Bukkit.getOnlinePlayers()) {
                    playerName.add(p.getName());
                }
                StringUtil.copyPartialMatches(args[1], playerName, completions);
            }
            else {
                if("maxThread".equalsIgnoreCase(args[2])){
                    return new ArrayList<>(Collections.singletonList("[起動可能スレッド数]"));
                }
                else {
                    StringUtil.copyPartialMatches(args[2], Arrays.asList("maxThread"), completions);
                }
            }
        }
        return completions;
    }
}
