package config;

import config.common.YamlConfigLoader;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.util.*;

public class Config {
    private static int jobsDisableTime = 0;
    private static double copyCost = 0.0;
    private static Material wand = null;
    private static String adminPermission = "";
    private static int defaultMaxThread = 0;
    private static String allowWorldName = "";
    private static int blockPlaceCooldown = 0;
    private static ArrayList<String> disableBlockSource = new ArrayList<>();
    private static final EnumSet<Material> disableBlocks = EnumSet.noneOf(Material.class);
    private static final HashSet<String> disableTextBlocks = new HashSet<>();
    private static ArrayList<String> replaceableBlockSource = new ArrayList<>();
    private static final EnumSet<Material> replaceableBlocks = EnumSet.noneOf(Material.class);
    private static final HashSet<String> replaceableTextBlocks = new HashSet<>();
    private static ArrayList<String> nonConsumableBlockSource = new ArrayList<>();
    private static final EnumSet<Material> nonConsumableBlocks = EnumSet.noneOf(Material.class);
    private static final HashSet<String> nonConsumableTextBlocks = new HashSet<>();

    private Config(){

    }

    public static int getJobsDisableTime() {
        return jobsDisableTime;
    }

    public static double getCopyCost() {
        return copyCost;
    }

    public static Material getWand() {
        return wand;
    }

    public static String getAdminPermission(){
        return adminPermission;
    }

    public static int getDefaultMaxThread(){
        return defaultMaxThread;
    }

    public static String getAllowWorldName(){
        return allowWorldName;
    }

    public static int getBlockPlaceCooldown(){
        return blockPlaceCooldown;
    }

    public static ArrayList<String> getDisableBlockSource() {
        return disableBlockSource;
    }

    public static ArrayList<String> getReplaceableBlockSource() {
        return replaceableBlockSource;
    }

    public static ArrayList<String> getNonConsumableBlockSource(){
        return nonConsumableBlockSource;
    }

    public static void refreshConfig(Plugin plugin) throws IOException {
        YamlConfigLoader config = new YamlConfigLoader(plugin);
        //JOBS_DISABLE_TIME
        jobsDisableTime = config.getValue("JOBS_DISABLE_TIME",Integer.class,0);
        config.setComments("JOBS_DISABLE_TIME",new ArrayList<>(Collections.singletonList("Jobsプラグイン無効時間設定(秒)")));
        //COPY_COST
        copyCost = config.getValue("COPY_COST",Double.class,0.0);
        config.setComments("COPY_COST",new ArrayList<>(Collections.singletonList("Copyモードの1ブロック当たりのコスト ※置換可能ブロックはコスト0")));
        //WAND
        wand = Material.getMaterial(config.getValue("WAND",String.class,"NETHER_STAR"));
        config.setComments("WAND",new ArrayList<>(Collections.singletonList("WAND指定アイテム")));
        //ADMIN_PERMISSION
        adminPermission = config.getValue("ADMIN_PERMISSION",String.class,"minecraft.admin");
        config.setComments("ADMIN_PERMISSION",new ArrayList<>(Collections.singletonList("管理者パーミッション")));
        //DEFAULT_MAX_THREAD
        defaultMaxThread = config.getValue("DEFAULT_MAX_THREAD",Integer.class,2);
        config.setComments("DEFAULT_MAX_THREAD",new ArrayList<>(Collections.singletonList("デフォルト使用可能スレッド(タスク)数")));
        //ALLOW_WORLD_NAME
        allowWorldName = config.getValue("ALLOW_WORLD_NAME",String.class,"world");
        config.setComments("ALLOW_WORLD_NAME",new ArrayList<>(Collections.singletonList("AutoFill使用可能ワールド名")));
        //BLOCK_PLACE_COOLDOWN
        blockPlaceCooldown = config.getValue("BLOCK_PLACE_COOLDOWN",Integer.class,50);
        config.setComments("BLOCK_PLACE_COOLDOWN",new ArrayList<>(Collections.singletonList("AutoFillブロック設置クールダウン(mill)")));
        //DISABLE_BLOCKS
        disableBlockSource = (ArrayList<String>)config.getListValue("DISABLE_BLOCKS",new ArrayList<>(Collections.singletonList("BARRIER")));
        config.setComments("DISABLE_BLOCKS",new ArrayList<>(Collections.singletonList("AutoFill禁止ブロック(リスト)")));
        setMaterialList(disableBlockSource,disableBlocks,disableTextBlocks);
        //REPLACEABLE_BLOCKS
        replaceableBlockSource = (ArrayList<String>)config.getListValue("REPLACEABLE_BLOCKS",new ArrayList<>(Collections.singletonList("AIR")));
        config.setComments("REPLACEABLE_BLOCKS",new ArrayList<>(Collections.singletonList("置換可能ブロック(リスト) ※置き換えられたブロックは消失します")));
        setMaterialList(replaceableBlockSource,replaceableBlocks,replaceableTextBlocks);
        //NON_CONSUMABLE_BLOCKS
        nonConsumableBlockSource = (ArrayList<String>)config.getListValue("NON_CONSUMABLE_BLOCKS",new ArrayList<>(Arrays.asList("AIR","WATER")));
        config.setComments("NON_CONSUMABLE_BLOCKS",new ArrayList<>(Collections.singletonList("AutoFill使用時消費されないブロック(リスト)")));
        setMaterialList(nonConsumableBlockSource,nonConsumableBlocks,nonConsumableTextBlocks);
    }

    private static void setMaterialList(ArrayList<String> source,EnumSet<Material> enumSet, HashSet<String> hashSet){
        enumSet.clear();
        hashSet.clear();
        for (String str: source) {
            Material material = Material.getMaterial(str, false);
            if(material != null) {
                enumSet.add(material);
            }
            else{
                material = Material.getMaterial(str, true);
                if(material != null) {
                    enumSet.add(material);
                }
                else{
                    hashSet.add(str);
                }
            }
        }
    }

    public static boolean isDisabledBlock(Material material){
        if(disableBlocks.contains(material)){
            return true;
        }
        for (String str: disableTextBlocks) {
            if(material.toString().matches(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReplaceableBlock(Material material){
        if(replaceableBlocks.contains(material)){
            return true;
        }
        for (String str: replaceableTextBlocks) {
            if(material.toString().matches(str)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNonConsumableBlock(Material material){
        if(nonConsumableBlocks.contains(material)){
            return true;
        }
        for (String str: nonConsumableTextBlocks) {
            if(material.toString().matches(str)) {
                return true;
            }
        }
        return false;
    }
}
