package command.argument.basecommand;

import command.common.CommandMethod;
import common.PluginUtil;
import common.Util;
import config.Config;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reload extends CommandMethod {
    public Reload(){
        this.argumentName = "reload";
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        if(p.hasPermission("autofill.reload")) {
            Util.reloadPlugin(PluginUtil.getPlugin());
            try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
                p.sendMessage(LanguageUtil.getWord(database.getPlayerStatus(p).getUsingLanguage(),"reloadMessage"));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
            p.sendMessage("autofillコンフィグがリロードされました");
            p.sendMessage("マテリアルが一つも一致しないものは赤く表示されます");
            String disableLists = "";
            String replaceableLists = "";
            for (String str : Config.getDisableBlocks()) {
                Material m = null;
                try{
                    m = Material.matchMaterial(str);
                }
                catch (Exception e){}
                if(m != null){
                    disableLists += "§a" + str + " , ";
                }
                else{
                    disableLists += "§c" + str + " , ";
                }
            }
            for (String str : Config.getReplaceableBlocks()) {
                Material m = null;
                try{
                    m = Material.matchMaterial(str);
                }
                catch (Exception e){}
                if(m != null){
                    replaceableLists += "§a" + str + " , ";
                }
                else{
                    replaceableLists += "§c" + str + " , ";
                }
            }
            p.sendMessage("禁止ブロックリスト: " + disableLists);
            p.sendMessage("上書き可能ブロックリスト: " + replaceableLists);
            p.sendMessage("Jobs無効時間: " + Config.getJobsBlockTimer() + "秒");
            p.sendMessage("Copyモードコスト(1ブロック当たり): " + Config.getCopyCost() + "MOFU");
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }
}
