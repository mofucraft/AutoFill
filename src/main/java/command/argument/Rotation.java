package command.argument;

import command.common.CommandMethod;
import database.PlayerStatusList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.minecraft.autofill.FillData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Rotation extends CommandMethod {
    public Rotation(){
        this.argumentName = "rotation";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        PlayerStatusList.checkUserData(p);
        FillData fillData = PlayerStatusList.getPlayerData(p);
        if (args.length > 1) {
            boolean setCheck = true;
            if(args[1].equalsIgnoreCase("0")){
                p.sendMessage("§8[§6AutoFill§8] §f回転方向を§aなし§fに設定しました");
                fillData.rotation = 0;
            }
            else if(args[1].equalsIgnoreCase("90")){
                p.sendMessage("§8[§6AutoFill§8] §f回転方向を時計回りに§a90度§fに設定しました");
                fillData.rotation = 90;
            }
            else if(args[1].equalsIgnoreCase("180")){
                p.sendMessage("§8[§6AutoFill§8] §f回転方向を時計回りに§a180度§fに設定しました");
                fillData.rotation = 180;
            }
            else if(args[1].equalsIgnoreCase("270")){
                p.sendMessage("§8[§6AutoFill§8] §f回転方向を時計回りに§a270度§fに設定しました");
                fillData.rotation = 270;
            }
            else{
                p.sendMessage("§8[§6AutoFill§8] §f回転方向は§a0,90,180,270§fの中から選択できます");
                setCheck = false;
            }
            if(setCheck){
                p.sendMessage("§8[§6AutoFill§8] §f再度コピー開始地点を確認してください");
                p.sendMessage("§8[§6AutoFill§8] §fまた、回転方向設定はコピーモード時のみ適用されます");
                //ShowRangeParticle(p);
            }
        } else {
            if(fillData.rotation == 0){
                p.sendMessage("§8[§6AutoFill§8] §f現在、回転方向は設定されていません");
            }
            else{
                p.sendMessage("§8[§6AutoFill§8] §f現在、回転方向は§a" + fillData.rotation + "度§fに設定されています");
            }
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        final String[] rotations = { "0","90","180","270" };
        return new ArrayList<>(Arrays.asList(rotations));
    }
}
