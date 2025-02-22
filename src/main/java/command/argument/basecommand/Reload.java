package command.argument.basecommand;

import command.common.CommandMethod;
import common.PluginUtil;
import common.InitializeUtil;
import config.Config;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.*;

public class Reload extends CommandMethod {
    public Reload(){
        super("reload",true,true);
    }

    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        InitializeUtil.reloadPlugin(PluginUtil.getPlugin());
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            Map<String, String> variables = new HashMap<>();
            variables.put("jobsDisableTime", Integer.toString(Config.getJobsDisableTime()));
            variables.put("copyCost", Double.toString(Config.getCopyCost()));
            variables.put("wand", Config.getWand().toString());
            variables.put("adminPermission", Config.getAdminPermission());
            variables.put("defaultMaxThread", Integer.toString(Config.getDefaultMaxThread()));
            variables.put("defaultUseThread", Integer.toString(Config.getDefaultUseThread()));
            variables.put("allowWorldName", Config.getAllowWorldName());
            variables.put("blockPlaceCooldown", Integer.toString(Config.getBlockPlaceCooldown()));
            variables.put("disableBlockList", this.getMatchMaterialList(Config.getDisableBlockSource()));
            variables.put("replaceableBlockList", this.getMatchMaterialList(Config.getReplaceableBlockSource()));
            variables.put("nonConsumableBlockList", this.getMatchMaterialList(Config.getNonConsumableBlockSource()));
            LanguageUtil.sendReplacedMessage(p,database.getPlayerStatus(p).getUsingLanguage(),"reload", variables);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }

    private String getMatchMaterialList(ArrayList<String> list){
        StringBuilder stringBuilder = new StringBuilder();
        for (String str : list) {
            Material m = Material.matchMaterial(str);
            if(m != null){
                stringBuilder.append("§a").append(str).append(" , ");
            }
            else{
                stringBuilder.append("§c").append(str).append(" , ");
            }
        }
        return stringBuilder.toString();
    }
}
