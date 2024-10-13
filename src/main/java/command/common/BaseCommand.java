package command.common;

import api.CoreProtectAPI;
import api.EconomyAPI;
import com.gamingmesh.jobs.Jobs;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.MetadataConstants;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import common.PluginUtil;
import config.Config;
import database.PlayerStatusList;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecraft.autofill.FillData;
import org.minecraft.autofill.FillMode;
import org.minecraft.autofill.Process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class BaseCommand extends CommandMethod {
    @Override
    public boolean process(CommandSender sender, Command command, String label, String[] args) {
        Player p = (Player)sender;
        if (p.getWorld().getName().equalsIgnoreCase("world")) {
            World world = getServer().getWorld("world");
            if (PlayerStatusList.containsKey(p)) {
                FillData fillData = PlayerStatusList.getPlayerData(p);
                if(fillData.position1 == null || fillData.position2 == null){
                    p.sendMessage("§8[§6AutoFill§8] §c選択ツール(" + Config.getWand().toString() + ")で範囲を選択してください");
                    return false;
                }
                if(!((fillData.copyPosition == null || fillData.position1.getWorld().getName().equalsIgnoreCase("world")) &&
                        (fillData.copyPosition == null || fillData.position2.getWorld().getName().equalsIgnoreCase("world")) &&
                        (fillData.copyPosition == null || fillData.copyPosition.getWorld().getName().equalsIgnoreCase("world")))){
                    p.sendMessage("§8[§6AutoFill§8] §cautofillは建築ワールドでしか使用できません");
                    return false;
                }
                if(Config.checkDisabledBlocks(fillData.blockData.getMaterial())) {
                    if(fillData.canBlockFill(p)) {
                        if(fillData.Mode == FillMode.Copy){
                            if(fillData.copyPosition == null){
                                p.sendMessage("§8[§6AutoFill§8] §cCopyモードの際はコピー先を/autofill csetを実行後に始点を設定アイテム(" + Config.getWand().toString() + ")でクリックして設定してください");
                                return false;
                            }
                        }
                        Location pos1 = new Location(null,Math.min(fillData.position1.getX(),fillData.position2.getX()),Math.min(fillData.position1.getY(),fillData.position2.getY()),Math.min(fillData.position1.getZ(),fillData.position2.getZ()));
                        Location pos2 = new Location(null,Math.max(fillData.position1.getX(),fillData.position2.getX()),Math.max(fillData.position1.getY(),fillData.position2.getY()),Math.max(fillData.position1.getZ(),fillData.position2.getZ()));
                        int Yc = (int) pos2.getY() - (int) pos1.getY();
                        int Xc = (int) pos2.getX() - (int) pos1.getX();
                        int Zc = (int) pos2.getZ() - (int) pos1.getZ();
                        int iMax = Math.abs(Yc) + 1;
                        int jMax = Math.abs(Xc) + 1;
                        int kMax = Math.abs(Zc) + 1;
                        Location copyPos = fillData.copyPosition;
                        BlockData setBlock = fillData.blockData;
                        Material setBlockMaterial = setBlock.getMaterial();
                        Yc = 1;
                        if (Xc != 0) Xc = Xc / Math.abs(Xc);
                        else Xc = 1;
                        if (Zc != 0) Zc = Zc / Math.abs(Zc);
                        else Zc = 1;
                        int finalYc = Yc;
                        int finalXc = Xc;
                        int finalZc = Zc;
                        p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                        p.sendMessage("autofillを開始します。§c/cancelfill§fで中止できます");
                        p.sendMessage("設置モード: §a" + fillData.Mode + "モード");
                        if(fillData.Mode == FillMode.Fill) {
                            p.sendMessage("設置ブロック: §a" + setBlockMaterial.toString());
                            int blocks = iMax * jMax * kMax;
                            p.sendMessage("総ブロック数: §a" + blocks + "ブロック(" + String.format("%.1f", ((double) blocks / 64)) + "スタック)");
                        }
                        else if(fillData.Mode == FillMode.Frame) {
                            p.sendMessage("設置ブロック: §a" + setBlockMaterial.toString());
                            int blocks = 0;
                            for (int i = 0; i < iMax; i++) {
                                for (int j = 0; j < jMax; j++) {
                                    for (int k = 0; k < kMax; k++) {
                                        Block b = world.getBlockAt((int) pos1.getX() + (j * finalXc),
                                                (int) pos1.getY() + (i * finalYc),
                                                (int) pos1.getZ() + (k * finalZc));
                                        if (fillData.Mode == FillMode.Frame) {
                                            int check = 0;
                                            if (b.getX() == (int) fillData.position1.getX() || b.getX() == (int) fillData.position2.getX()) {
                                                check++;
                                            }
                                            if (b.getY() == (int) fillData.position1.getY() || b.getY() == (int) fillData.position2.getY()) {
                                                check++;
                                            }
                                            if (b.getZ() == (int) fillData.position1.getZ() || b.getZ() == (int) fillData.position2.getZ()) {
                                                check++;
                                            }
                                            if (check < 2) {
                                                continue;
                                            }
                                            blocks++;
                                        }
                                    }
                                }
                            }
                            p.sendMessage("総ブロック数: §a" + blocks + "ブロック(" + String.format("%.1f", ((double) blocks / 64)) + "スタック)");
                        }
                        else if(fillData.Mode == FillMode.Copy){
                            if(fillData.rotation == 0){
                                p.sendMessage("回転: §aなし");
                            }
                            else{
                                p.sendMessage("回転: §a" + fillData.rotation + "度");
                            }
                            p.sendMessage("コピーモードの際はコピー元のブロックと同じブロックが要求されます");
                        }
                        UUID threadID = UUID.randomUUID();
                        fillData.thread.put(threadID, new org.minecraft.autofill.Process(true));
                        Process process = fillData.thread.get(threadID);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
                                RegionManager regions = container.get(BukkitAdapter.adapt(world));
                                Sound bSound = setBlockMaterial.createBlockData().getSoundGroup().getPlaceSound();
                                ArrayList<ArrayList<ArrayList<BlockData>>> layer1 = new ArrayList<>();
                                for (int i = 0; i < iMax; i++) {
                                    ArrayList<ArrayList<BlockData>> layer2 = new ArrayList<>();
                                    for (int j = 0; j < jMax; j++) {
                                        ArrayList<BlockData> layer3 = new ArrayList<>();
                                        for (int k = 0; k < kMax; k++) {
                                            if(p.isOnline() == false) process.placing = false;
                                            if (process.placing == true) {
                                                try {
                                                    Block b = world.getBlockAt((int) pos1.getX() + (j * finalXc),
                                                            (int) pos1.getY() + (i * finalYc),
                                                            (int) pos1.getZ() + (k * finalZc));
                                                    if(fillData.Mode == FillMode.Copy){
                                                        layer3.add(b.getBlockData().clone());
                                                        continue;
                                                    }
                                                    if(fillData.Mode == FillMode.Frame){
                                                        int check = 0;
                                                        if(b.getX() == (int)fillData.position1.getX() || b.getX() == (int)fillData.position2.getX()){
                                                            check++;
                                                        }
                                                        if(b.getY() == (int)fillData.position1.getY() || b.getY() == (int)fillData.position2.getY()){
                                                            check++;
                                                        }
                                                        if(b.getZ() == (int)fillData.position1.getZ() || b.getZ() == (int)fillData.position2.getZ()){
                                                            check++;
                                                        }
                                                        if(check < 2){
                                                            continue;
                                                        }
                                                    }
                                                    boolean canBuild = canBuilt(regions, b.getLocation(), p);
                                                    if (!Config.checkReplaceableBlocks(b.getBlockData().getMaterial()) && canBuild ) {
                                                        Inventory inv = p.getInventory();
                                                        if (inv.contains(setBlockMaterial) || p.hasPermission("mofucraft.staff")) {
                                                            if(!p.hasPermission("mofucraft.staff")) {
                                                                int slot = inv.first(setBlockMaterial);
                                                                ItemStack item = inv.getItem(slot);
                                                                item.setAmount(item.getAmount() - 1);
                                                                p.getInventory().setItem(slot, item);
                                                            }
                                                            Location bLoc = b.getLocation();
                                                            if (CoreProtectAPI.getAPI() != null) {
                                                                CoreProtectAPI.getAPI().logPlacement(p.getName(), b.getLocation(), setBlockMaterial, null);
                                                            }
                                                            setUnnaturalBlock(b);
                                                            setType(b, setBlock);
                                                            if(Config.getJobsBlockTimer() != 0) {
                                                                Jobs.getBpManager().add(b, Config.getJobsBlockTimer());
                                                            }
                                                            getServer().getOnlinePlayers().forEach(player -> {
                                                                player.playSound(bLoc, bSound, 1, 1);
                                                            });
                                                            Thread.sleep(50);
                                                        } else {
                                                            p.sendMessage("§8[§6AutoFill§8] §cブロックが足りなくなったためautofillを終了します");
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
                                        if(fillData.Mode == FillMode.Copy){
                                            layer2.add(layer3);
                                        }
                                        if (process.placing == false) break;
                                    }
                                    if(fillData.Mode == FillMode.Copy){
                                        layer1.add(layer2);
                                    }
                                    if (process.placing == false) break;
                                }
                                if(fillData.Mode == FillMode.Copy){
                                    for (int i = 0; i < iMax; i++) {
                                        ArrayList<ArrayList<BlockData>> l2 = layer1.get(i);
                                        for (int j = 0; j < jMax; j++) {
                                            ArrayList<BlockData> l3 = l2.get(j);
                                            for (int k = 0; k < kMax; k++) {
                                                BlockData copyBlock = l3.get(k);
                                                Material copyMaterial = Material.matchMaterial(copyBlock.getMaterial().getTranslationKey().replace("block.minecraft.",""));
                                                if(!Config.checkReplaceableBlocks(copyBlock.getMaterial())){
                                                    continue;
                                                }
                                                if(p.isOnline() == false) process.placing = false;
                                                if (process.placing == true) {
                                                    try {
                                                        Block b;
                                                        if(fillData.rotation == 0){
                                                            b = world.getBlockAt((int) copyPos.getX() + (j * finalXc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (k * finalZc));
                                                        }
                                                        else if(fillData.rotation == 90){
                                                            b = world.getBlockAt((int) copyPos.getX() + (k * finalZc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (j * finalXc));
                                                            copyBlock.rotate(StructureRotation.CLOCKWISE_90);
                                                        }
                                                        else if(fillData.rotation == 180){
                                                            b = world.getBlockAt((int) copyPos.getX() + (jMax - 1) - (j * finalXc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (kMax - 1) - (k * finalZc));
                                                            copyBlock.rotate(StructureRotation.CLOCKWISE_180);
                                                        }
                                                        else{
                                                            b = world.getBlockAt((int) copyPos.getX() + (kMax - 1) - (k * finalZc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (jMax - 1) - (j * finalXc));
                                                            copyBlock.rotate(StructureRotation.COUNTERCLOCKWISE_90);
                                                        }
                                                        Sound copyBlockSound = copyMaterial.createBlockData().getSoundGroup().getPlaceSound();
                                                        boolean canBuild = canBuilt(regions, b.getLocation(), p);
                                                        if (!Config.checkReplaceableBlocks(b.getBlockData().getMaterial()) && canBuild ) {
                                                            Inventory inv = p.getInventory();
                                                            if(EconomyAPI.getAPI().getBalance(p) < Config.getCopyCost()){
                                                                p.sendMessage("§8[§6AutoFill§8] §c所持金が不足しているためautofillを終了します");
                                                                process.placing = false;
                                                                continue;
                                                            }
                                                            if (inv.contains(copyMaterial) || p.hasPermission("mofucraft.staff")) {
                                                                if(!p.hasPermission("mofucraft.staff")) {
                                                                    int slot = inv.first(copyMaterial);
                                                                    ItemStack item = inv.getItem(slot);
                                                                    item.setAmount(item.getAmount() - 1);
                                                                    p.getInventory().setItem(slot, item);
                                                                }
                                                                Location bLoc = b.getLocation();
                                                                if (CoreProtectAPI.getAPI() != null) {
                                                                    CoreProtectAPI.getAPI().logPlacement(p.getName(), b.getLocation(), copyMaterial, null);
                                                                }
                                                                setUnnaturalBlock(b);
                                                                setType(b, copyBlock);
                                                                if(Config.getJobsBlockTimer() != 0) {
                                                                    Jobs.getBpManager().add(b, Config.getJobsBlockTimer());
                                                                }
                                                                EconomyAPI.getAPI().withdrawPlayer(p, Config.getCopyCost());
                                                                getServer().getOnlinePlayers().forEach(player -> {
                                                                    player.playSound(bLoc, copyBlockSound, 1, 1);
                                                                });
                                                                Thread.sleep(50);
                                                            } else {
                                                                p.sendMessage("§8[§6AutoFill§8] §cコピー元のブロック(" + copyMaterial.toString() + ")がインベントリに存在しないためautofillを終了します");
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
                                }
                                if (process.placing != false) {
                                    p.sendMessage("§8[§6AutoFill§8] §aautofillが完了しました");
                                }
                                fillData.thread.remove(threadID);
                            }
                        }).start();
                    }
                } else {
                    p.sendMessage("§8[§6AutoFill§8] §cこのブロックはautofillで設置できません");
                }
            }
            else{
                PlayerStatusList.checkUserData(p);
                p.sendMessage("§8[§6AutoFill§8] §c選択ツール(" + Config.getWand().toString() + ")で範囲を選択してください");
            }
        } else {
            p.sendMessage("§8[§6AutoFill§8] §cautofillは建築ワールドでしか使用できません");
        }
        return true;
    }

    @Override
    public List<String> tabCompleterProcess(CommandSender commandSender, Command command, String label, String[] args) {
        return new ArrayList<>(Collections.singletonList(""));
    }

    public void setType(final Block b, final BlockData bd){
        new BukkitRunnable() {
            public void run() {
                b.setBlockData(bd);
                b.getState().update(true, true);
            }
        }.runTask(PluginUtil.getPlugin());
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

    public static void setUnnaturalBlock(@NotNull Block block) {
        mcMMO.getPlaceStore().setTrue(block);

        // Failsafe against lingering metadata
        if(block.hasMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS))
            block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, mcMMO.p);
    }
}
