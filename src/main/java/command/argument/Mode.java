package command.argument;

import command.common.CommandMethod;
import config.Config;
import database.PlayerStatusList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.minecraft.autofill.FillData;
import org.minecraft.autofill.FillMode;

import java.util.ArrayList;
import java.util.List;

public class Mode extends CommandMethod {
    public Mode(){
        this.argumentName = "mode";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        PlayerStatusList.checkUserData(p);
        FillData fillData = PlayerStatusList.getPlayerData(p);
        if (args.length > 1) {
            FillMode mode = null;
            for(FillMode modes:FillMode.values()){
                if(args[1].equalsIgnoreCase(modes.getString())){
                    mode = modes;
                }
            }
            if(mode != null){
                fillData.Mode = mode;
                p.sendMessage("§8[§6AutoFill§8] §a" + mode.getString() + "モード§fに変更しました");
                if(mode == FillMode.Fill){
                    p.sendMessage("§8[§6AutoFill§8] §fFillモードでは選択範囲内部を選択したブロックで埋め立てます");
                }
                else if(mode == FillMode.Frame){
                    p.sendMessage("§8[§6AutoFill§8] §fFrameモードでは選択範囲内部に枠組みを作成します");
                }
                else if(mode == FillMode.Copy){
                    p.sendMessage("§8[§6AutoFill§8] §fCopyモードではコピーするブロック1つにつきそのコピー元のブロックと" + Config.getCopyCost() + "MOFUが必要となります");
                    p.sendMessage("§8[§6AutoFill§8] §f範囲指定後、/autofill csetでコピー先始点を指定してください");
                }
            }
            else{
                StringBuilder modeList = new StringBuilder();
                boolean first = false;
                for(FillMode modes:FillMode.values()){
                    if(!first) {
                        modeList.append(modes.getString());
                        first = true;
                    }
                    else{
                        modeList.append(",").append(modes.getString());
                    }
                }
                p.sendMessage("§8[§6AutoFill§8] §f現在設定可能なモードは§a" + modeList + "§fです");
            }
        } else {
            p.sendMessage("§8[§6AutoFill§8] §f現在は§a" + fillData.Mode + "モード§fに設定されています");
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> modeList = new ArrayList<>();
        for(FillMode modes:FillMode.values()){
            completions.add(modes.getString());
        }
        StringUtil.copyPartialMatches(args[0], modeList, completions);
        return completions;
    }
}
