package command.argument;

import command.common.CommandMethod;
import database.list.PlayerStatusList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.UserData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cancel extends CommandMethod {
    public Cancel(){
        this.argumentName = "cancel";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        PlayerStatusList.checkUserData(p);
        boolean stop = false;
        UserData userData = PlayerStatusList.getPlayerData(p);
        if (!userData.getThread().isEmpty()) stop = true;
        userData.getThread().forEach((uuid, process) -> {
            process.placing = false;
        });
        if (stop) {
            p.sendMessage("autofillをキャンセルしました");
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
