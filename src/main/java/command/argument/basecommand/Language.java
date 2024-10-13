package command.argument.basecommand;

import command.common.CommandMethod;
import database.PlayerStatus;
import database.StatusDatabase;
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
        this.argumentName = "language";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        try(StatusDatabase database = new StatusDatabase()){
            if(args.length < 2) {
                p.sendMessage(LanguageUtil.getWord(database.getPlayerStatus(p).getUsingLanguage(), "notEnoughArgumentMessage"));
                return false;
            }
            if(!LanguageUtil.getLanguageList().contains(args[1])){
                p.sendMessage(LanguageUtil.getWord(database.getPlayerStatus(p).getUsingLanguage(), "notExistsLanguageMessage"));
                return false;
            }
            PlayerStatus playerStatus = database.setLanguage(p, args[1]);
            Map<String, String> variables = new HashMap<>();
            variables.put("language", playerStatus.getUsingLanguage());
            p.sendMessage(LanguageUtil.getReplacedWord(database.getPlayerStatus(p).getUsingLanguage(),"languageSetMessage", variables));
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