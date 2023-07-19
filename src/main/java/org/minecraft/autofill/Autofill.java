package org.minecraft.autofill;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.util.Direction;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Autofill extends JavaPlugin implements Listener {

    public Map<UUID,FillData> playerData = new Hashtable<>();
    public List<String> disableBlocks = new ArrayList<>();

    public CoreProtectAPI cApi;

    public WorldGuard wApi;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        cApi = getCoreProtect();
        wApi = WorldGuard.getInstance();
        loadConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.hasItem() && e.getItem().getType() == Material.NETHER_STAR){
            if(!playerData.containsKey(p.getUniqueId())){
                playerData.put(p.getUniqueId(),new FillData());
            }
            FillData fillData = playerData.get(p.getUniqueId());
            if(e.getAction() == Action.LEFT_CLICK_BLOCK){
                p.sendMessage("第1ポジションを設定しました(§aX:" + (int)e.getClickedBlock().getLocation().getX() +
                        " Y:" + (int)e.getClickedBlock().getLocation().getY() +
                        " Z:" + (int)e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofill コマンドで一括設置できます");
                fillData.position1 = e.getClickedBlock().getLocation();
                fillData.blockData = e.getClickedBlock().getBlockData().getMaterial();
            }
            else if(e.getAction() == Action.RIGHT_CLICK_BLOCK){
                p.sendMessage("第2ポジションを設定しました(§aX:" + (int)e.getClickedBlock().getLocation().getX() +
                        " Y:" + (int)e.getClickedBlock().getLocation().getY() +
                        " Z:" + (int)e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofill コマンドで一括設置できます");
                fillData.position2 = e.getClickedBlock().getLocation();
            }
            playerData.put(p.getUniqueId(),fillData);
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = getServer().getPlayer(sender.getName());
        if(command.getName().equalsIgnoreCase("autofill")){
            if(p.getWorld().getName().equalsIgnoreCase("world")) {
                World world = getServer().getWorld("world");
                if (playerData.containsKey(p.getUniqueId())) {
                    FillData fillData = playerData.get(p.getUniqueId());
                    if(checkDisabledBlocks(fillData.blockData)) {
                        if (fillData.canBlockFill(p)) {
                            if (fillData.position1.getY() > fillData.position2.getY()) {
                                Location temp = fillData.position1;
                                fillData.position1 = fillData.position2;
                                fillData.position2 = temp;
                            }
                            int Yc = (int) fillData.position2.getY() - (int) fillData.position1.getY();
                            int Xc = (int) fillData.position2.getX() - (int) fillData.position1.getX();
                            int Zc = (int) fillData.position2.getZ() - (int) fillData.position1.getZ();
                            int iMax = Math.abs(Yc) + 1;
                            int jMax = Math.abs(Xc) + 1;
                            int kMax = Math.abs(Zc) + 1;
                            Location pos1 = fillData.position1;
                            Material setBlock = fillData.blockData;
                            Yc = 1;
                            if (Xc != 0) Xc = Xc / Math.abs(Xc);
                            else Xc = 1;
                            if (Zc != 0) Zc = Zc / Math.abs(Zc);
                            else Zc = 1;
                            int finalYc = Yc;
                            int finalXc = Xc;
                            int finalZc = Zc;
                            p.sendMessage("autofillを開始します。§c/cancelfill§fで中止できます");
                            p.sendMessage("設置ブロック: §a" + setBlock.toString());
                            int blocks = iMax * jMax * kMax;
                            p.sendMessage("総ブロック数: §a" + blocks + "ブロック(" + String.format("%.1f", ((double) blocks / 64)) + "スタック)");
                            UUID threadID = UUID.randomUUID();
                            fillData.thread.put(threadID,new Process(true));
                            Process process = fillData.thread.get(threadID);
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                    RegionManager regions = container.get(BukkitAdapter.adapt(world));
                                    Sound bSound = setBlock.createBlockData().getSoundGroup().getPlaceSound();
                                    for (int i = 0; i < iMax; i++) {
                                        for (int j = 0; j < jMax; j++) {
                                            for (int k = 0; k < kMax; k++) {
                                                if (process.placing == true) {
                                                    try {
                                                        Block b = world.getBlockAt((int) pos1.getX() + (j * finalXc),
                                                                (int) pos1.getY() + (i * finalYc),
                                                                (int) pos1.getZ() + (k * finalZc));
                                                        boolean canBuild = canBuilt(regions, b.getLocation(), p);
                                                        if (b.isEmpty() && canBuild) {
                                                            Inventory inv = p.getInventory();
                                                            if (inv.contains(setBlock)) {
                                                            //if (inv.contains(setBlock) || p.getGameMode() == GameMode.CREATIVE) {
                                                                //if(p.getGameMode() != GameMode.CREATIVE) {
                                                                    int slot = inv.first(setBlock);
                                                                    ItemStack item = inv.getItem(slot);
                                                                    item.setAmount(item.getAmount() - 1);
                                                                    p.getInventory().setItem(slot, item);
                                                                //}
                                                                Location bLoc = b.getLocation();
                                                                if (cApi != null) {
                                                                    cApi.logPlacement(p.getName(), b.getLocation(), setBlock, null);
                                                                }
                                                                setType(b, setBlock);
                                                                getServer().getOnlinePlayers().forEach(player -> {
                                                                    player.playSound(bLoc, bSound, 1, 1);
                                                                });
                                                                Thread.sleep(50);
                                                            } else {
                                                                p.sendMessage("ブロックが足りないためautofillを終了します");
                                                                process.placing = false;
                                                            }
                                                        } else {
                                                            Thread.sleep(2);
                                                        }
                                                    } catch (InterruptedException e) {
                                                        System.out.println(e.getMessage());
                                                    }
                                                }
                                                if (process.placing == false) break;
                                            }
                                            if (process.placing == false) break;
                                        }
                                        if (process.placing == false) break;
                                    }
                                    if (process.placing != false) {
                                        p.sendMessage("autofillが完了しました");
                                    }
                                    fillData.thread.remove(threadID);
                                }
                            }).start();
                        }
                    }
                    else{
                        p.sendMessage("このブロックはautofillで設置できません");
                    }
                }
            }
            else{
                p.sendMessage("autofillは建築ワールドでしか使用できません");
            }
        }
        if(command.getName().equalsIgnoreCase("cancelfill")){
            if(!playerData.containsKey(p.getUniqueId())){
                playerData.put(p.getUniqueId(),new FillData());
            }
            boolean stop = false;
            FillData fillData = playerData.get(p.getUniqueId());
            if(fillData.thread.size() > 0) stop = true;
            fillData.thread.forEach((uuid, process) -> {
                process.placing = false;
            });
            if(stop == true){
                p.sendMessage("autofillをキャンセルしました");
            }
        }
        return false;
    }

    public void setType(final Block b, final Material m){
        new BukkitRunnable() {
            public void run() {
                b.setType(m);
            }
        }.runTask(this);
    }

    private CoreProtectAPI getCoreProtect() {
        Plugin plugin = getServer().getPluginManager().getPlugin("CoreProtect");

        // Check that CoreProtect is loaded
        if (plugin == null || !(plugin instanceof CoreProtect)) {
            return null;
        }

        // Check that the API is enabled
        CoreProtectAPI CoreProtect = ((CoreProtect) plugin).getAPI();
        if (CoreProtect.isEnabled() == false) {
            return null;
        }

        // Check that a compatible version of the API is loaded
        if (CoreProtect.APIVersion() < 9) {
            return null;
        }

        return CoreProtect;
    }

    private boolean canBuilt(RegionManager regionManager, Location location, Player player) {
        if(player.hasPermission("mofucraft.staff")) return true;
        BlockVector3 position = BlockVector3.at(location.getX(),location.getY(),location.getZ());
        ApplicableRegionSet set = regionManager.getApplicableRegions(position);
        ProtectedRegion current = null;
        int priorityLevel = -2147483648;
        for (ProtectedRegion pr: set) {
            if(priorityLevel <= pr.getPriority()){
                current = pr;
            }
        }
        if(current == null) return true;
        if(current.getMembers().contains(player.getUniqueId()) || current.getOwners().contains(player.getUniqueId())){
            return true;
        }
        else{
            return false;
        }
    }

    private void loadConfig(){
        File userdata = Bukkit.getServer().getPluginManager().getPlugin("autofill").getDataFolder();
        try {
            File configFile = new File(userdata, File.separator + "config.yml");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
//            Set set = config.getConfigurationSection("AUTOFILL").getKeys(false);
//            java.lang.System.out.println(set.size());
//            set.forEach(key ->{
//                java.lang.System.out.println(key);
//            }
            if(config.getStringList("DISABLE_BLOCKS").size() == 0) {
                config.createSection("DISABLE_BLOCKS");
                List<String> defaultList = new ArrayList<>();
                defaultList.add("BARRIER");
                config.set("DISABLE_BLOCKS", defaultList);
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    java.lang.System.out.println(e.getMessage());
                }
            }
            for (String str:config.getStringList("DISABLE_BLOCKS")) {
                disableBlocks.add(str);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean checkDisabledBlocks(Material material){
        for (String str: disableBlocks) {
            if(material.toString().equalsIgnoreCase(str) || material.toString().matches(str)) {
                return false;
            }
        }
        return true;
    }
}
