package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.minecraft.autofill.UserData;
import org.minecraft.autofill.FillMode;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Mode extends CommandMethod {
    public Mode(){
        super("mode",false,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            if (args.length > 1) {
                FillMode mode = null;
                for(FillMode modes:FillMode.values()){
                    if(args[1].equalsIgnoreCase(modes.toString())){
                        mode = modes;
                    }
                }
                if(mode != null){
                    if (userData.getStructure() != null){
                        LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(),"discardStructureDataByChangeMode");
                        userData.setStructure(null);
                    }
                    userData.setMode(mode);
                    Map<String, String> variables = new HashMap<>();
                    variables.put("fillMode", mode.toString());
                    LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"changeFillMode", variables);
                    if(mode == FillMode.FILL){
                        LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "changeFillModeToFill");
                    }
                    else if(mode == FillMode.FRAME){
                        LanguageUtil.sendMessage(p, playerStatus.getUsingLanguage(), "changeFillModeToFrame");
                    }
                    else if(mode == FillMode.COPY){
                        Map<String, String> variables2 = new HashMap<>();
                        variables2.put("copyCost", Double.toString(Config.getCopyCost()));
                        LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"changeFillModeToCopy", variables2);
                    }
                }
                else{
                    StringBuilder modeList = new StringBuilder();
                    boolean first = false;
                    for(FillMode modes:FillMode.values()){
                        if(!first) {
                            modeList.append(modes.toString());
                            first = true;
                        }
                        else{
                            modeList.append(",").append(modes.toString());
                        }
                    }
                    Map<String, String> variables = new HashMap<>();
                    variables.put("modeList", modeList.toString());
                    LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"showAllFillMode", variables);
                }
            } else {
                Map<String, String> variables = new HashMap<>();
                variables.put("fillMode", userData.getMode().toString());
                LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"showCurrentFillMode", variables);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> modeList = new ArrayList<>();
        for(FillMode modes:FillMode.values()){
            completions.add(modes.toString());
        }
        StringUtil.copyPartialMatches(args[0], modeList, completions);
        return completions;
    }
}
