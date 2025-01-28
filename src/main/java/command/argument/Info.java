package command.argument;

import command.common.CommandMethod;
import database.PlayerStatus;
import database.PlayerStatusDatabase;
import database.list.PlayerStatusList;
import language.LanguageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.FillTask;
import org.minecraft.autofill.UserData;

import java.sql.SQLException;
import java.util.*;

public class Info extends CommandMethod {
    public Info(){
        super("info",false);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        UserData userData = PlayerStatusList.getPlayerData(p);
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            PlayerStatus playerStatus = database.getPlayerStatus(p);
            StringBuilder stringBuilder = new StringBuilder();
            for(int i = 0; i<playerStatus.getMaxThread(); i++){
                if(userData.getFillTaskList().size() <= i){
                    stringBuilder.append("  §7Thread").append(String.format("%02d", i + 1)).append("§7: §8|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||| §aReady");
                }
                else{
                    FillTask fillTask = userData.getFillTaskList().get(i);
                    if(fillTask.isPlacing()) {
                        boolean reach = true;
                        stringBuilder.append("  §aThread").append(String.format("%02d", i + 1)).append("§7: §a");
                        for (int j = 0; j < 100; j++) {
                            if (fillTask.getProgress() > j) {
                                stringBuilder.append("|");
                            }
                            else{
                                if(reach){
                                    stringBuilder.append("§8|");
                                    reach = false;
                                }else{
                                    stringBuilder.append("|");
                                }
                            }
                        }
                        stringBuilder.append(" §6").append(fillTask.getProgress()).append("%");
                    }
                    else{
                        stringBuilder.append("  §7Thread").append(String.format("%02d", i + 1)).append("§7: §8|||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||| §aReady");
                    }
                }
                if(i!=playerStatus.getMaxThread()){
                    stringBuilder.append("\n");
                }
            }
            Map<String, String> variables = new HashMap<>();
            variables.put("maxThread", Integer.toString(playerStatus.getMaxThread()));
            variables.put("usingThread", Integer.toString(userData.getPlacingThread()));
            variables.put("threadStatus", stringBuilder.toString());
            LanguageUtil.sendReplacedMessage(p, playerStatus.getUsingLanguage(),"information", variables);
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
