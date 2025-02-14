package command.argument.basecommand;

import command.common.CommandMethod;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Language extends CommandMethod {
    public Language(){
        super("language",false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            if(args.length < 2) {
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "notEnoughArgument");
                return false;
            }
            if(!LanguageUtil.getLanguageList().contains(args[1])){
                LanguageUtil.sendMessage(p, database.getPlayerStatus(p).getUsingLanguage(), "notExistsLanguage");
                return false;
            }
            PlayerStatus playerStatus = database.setLanguage(p, args[1]);
            Map<String, String> variables = new HashMap<>();
            variables.put("language", playerStatus.getUsingLanguage());
            LanguageUtil.sendReplacedMessage(p, database.getPlayerStatus(p).getUsingLanguage(),"languageSet", variables);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        if(args.length >= 2) StringUtil.copyPartialMatches(args[1], LanguageUtil.getLanguageList(), completions);
        return completions;
    }
}