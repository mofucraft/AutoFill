package config;

import config.common.YamlConfigLoader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static common.YamlUtil.getFirstPathList;

public class Config {
    private static int jobsBlockTimer = 0;
    private static double copyCost = 0.0;
    private static Material wand = null;
    private static ArrayList<String> disableBlocks = new ArrayList<>();
    private static ArrayList<String> replaceableBlocks = new ArrayList<>();

    private Config(){

    }

    public static ArrayList<String> getDisableBlocks() {
        return disableBlocks;
    }

    public static ArrayList<String> getReplaceableBlocks() {
        return replaceableBlocks;
    }

    public static int getJobsBlockTimer() {
        return jobsBlockTimer;
    }

    public static double getCopyCost() {
        return copyCost;
    }

    public static Material getWand() {
        return wand;
    }

    public static void refreshConfig(Plugin plugin) throws IOException {
        YamlConfigLoader config = new YamlConfigLoader(plugin);
        jobsBlockTimer = config.getValue("JOBS_BLOCK_TIMER",Integer.class,0);
        config.setComments("JOBS_BLOCK_TIMER",new ArrayList<>(Collections.singletonList("Jobsプラグイン無効時間設定(秒)")));
        copyCost = config.getValue("COPY_COST",Double.class,0.0);
        config.setComments("COPY_COST",new ArrayList<>(Collections.singletonList("Copyモードの1ブロック当たりのコスト(MOFU) ※置換可能ブロックはコスト0")));
        String tempWand = config.getValue("WAND",String.class,"NETHER_STAR");
        if(Material.getMaterial(tempWand) != null){
            wand = Material.NETHER_STAR;
        }
        else{
            wand = Material.getMaterial(tempWand);
        }
        config.setComments("WAND",new ArrayList<>(Collections.singletonList("WAND指定アイテム")));
        disableBlocks = (ArrayList<String>)config.getListValue("DISABLE_BLOCKS",new ArrayList<>(Collections.singletonList("BARRIER")));
        config.setComments("DISABLE_BLOCKS",new ArrayList<>(Collections.singletonList("AutoFill禁止ブロック設定")));
        replaceableBlocks = (ArrayList<String>)config.getListValue("REPLACEABLE_BLOCKS",new ArrayList<>(Collections.singletonList("AIR")));
        config.setComments("REPLACEABLE_BLOCKS",new ArrayList<>(Collections.singletonList("置換可能ブロック設定(リスト) ※置き換えられたブロックは消失します")));
    }

    public static boolean checkDisabledBlocks(Material material){
        for (String str: disableBlocks) {
            if(material.toString().equalsIgnoreCase(str) || material.toString().matches(str)) {
                return false;
            }
        }
        return true;
    }

    public static boolean checkReplaceableBlocks(Material material){
        for (String str: replaceableBlocks) {
            if(material.toString().equalsIgnoreCase(str) || material.toString().matches(str)) {
                return false;
            }
        }
        return true;
    }
}
