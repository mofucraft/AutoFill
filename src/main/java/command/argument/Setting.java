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

public class Setting extends CommandMethod {
    public Setting(){
        super("setting",false,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player) sender;
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            if(args.length < 3){
                LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "notEnoughArgument");
                return false;
            }
            if("useThread".equalsIgnoreCase(args[1])){
                if(NumberUtils.isNumber(args[2])) {
                    int newThreadCount = Integer.parseInt(args[2]);
                    if(newThreadCount < 1 || newThreadCount > playerStatus.getMaxThread()){
                        Map<String, String> variables = new HashMap<>();
                        variables.put("maxThread", Integer.toString(playerStatus.getMaxThread()));
                        LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"exceedMaximumThread", variables);
                        return false;
                    }
                    int oldThreadCount = playerStatus.getUseThread();
                    playerStatus.setUseThread(newThreadCount);
                    database.updatePlayerStatus(playerStatus);
                    Map<String, String> variables = new HashMap<>();
                    variables.put("name", playerStatus.getName());
                    variables.put("variableName","UseThread");
                    variables.put("oldValue",Integer.toString(oldThreadCount));
                    variables.put("value",Integer.toString(playerStatus.getUseThread()));
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
            if("useThread".equalsIgnoreCase(args[1])){
                return new ArrayList<>(Collections.singletonList("[スレッド数未指定時起動スレッド数]"));
            }
            else {
                StringUtil.copyPartialMatches(args[1], Arrays.asList("useThread"), completions);
            }
        }
        return completions;
    }
}
