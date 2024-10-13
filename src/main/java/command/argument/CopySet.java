package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatusList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.FillData;
import org.minecraft.autofill.SelectMode;

import java.util.*;

public class CopySet extends CommandMethod {
    public CopySet(){
        this.argumentName = "cset";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        PlayerStatusList.checkUserData(p);
        FillData fillData = PlayerStatusList.getPlayerData(p);
        p.sendMessage("§8[§6AutoFill§8] §fコピー先始点を選択ツール(" + Config.getWand().toString() + ")で設定してください");
        p.sendMessage("§8[§6AutoFill§8] §f左クリックでクリックしたブロック、右クリックでクリックしたブロックの面の上のブロックを選択できます");
        fillData.selectMode = SelectMode.Copy;
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
