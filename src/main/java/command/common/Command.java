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
import database.list.PlayerStatusList;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.minecraft.autofill.UserData;
import org.minecraft.autofill.FillMode;
import org.minecraft.autofill.Process;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.bukkit.Bukkit.getServer;

public class Command extends CommandMethod {
    @Override
    public boolean process(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        Player p = (Player)sender;
        if (p.getWorld().getName().equalsIgnoreCase("world")) {
            World world = getServer().getWorld("world");
            if (PlayerStatusList.containsKey(p)) {
                UserData userData = PlayerStatusList.getPlayerData(p);
                if(userData.getFirstPosition() == null || userData.getSecondPosition() == null){
                    p.sendMessage("§8[§6AutoFill§8] §c選択ツール(" + Config.getWand().toString() + ")で範囲を選択してください");
                    return false;
                }
                if(!((userData.getCopyPosition() == null || userData.getFirstPosition().getWorld().getName().equalsIgnoreCase("world")) &&
                        (userData.getCopyPosition() == null || userData.getSecondPosition().getWorld().getName().equalsIgnoreCase("world")) &&
                        (userData.getCopyPosition() == null || userData.getCopyPosition().getWorld().getName().equalsIgnoreCase("world")))){
                    p.sendMessage("§8[§6AutoFill§8] §cautofillは建築ワールドでしか使用できません");
                    return false;
                }
                if(Config.checkDisabledBlocks(userData.getBlockData().getMaterial())) {
                    if(userData.canBlockFill(p)) {
                        if(userData.getMode() == FillMode.COPY){
                            if(userData.getCopyPosition() == null){
                                p.sendMessage("§8[§6AutoFill§8] §cCopyモードの際はコピー先を/autofill csetを実行後に始点を設定アイテム(" + Config.getWand().toString() + ")でクリックして設定してください");
                                return false;
                            }
                        }
                        Location pos1 = new Location(null,Math.min(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()),Math.min(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()),Math.min(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                        Location pos2 = new Location(null,Math.max(userData.getFirstPosition().getX(), userData.getSecondPosition().getX()),Math.max(userData.getFirstPosition().getY(), userData.getSecondPosition().getY()),Math.max(userData.getFirstPosition().getZ(), userData.getSecondPosition().getZ()));
                        int Yc = (int) pos2.getY() - (int) pos1.getY();
                        int Xc = (int) pos2.getX() - (int) pos1.getX();
                        int Zc = (int) pos2.getZ() - (int) pos1.getZ();
                        int iMax = Math.abs(Yc) + 1;
                        int jMax = Math.abs(Xc) + 1;
                        int kMax = Math.abs(Zc) + 1;
                        Location copyPos = userData.getCopyPosition();
                        BlockData setBlock = userData.getBlockData();
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
                        p.sendMessage("設置モード: §a" + userData.getMode() + "モード");
                        if(userData.getMode() == FillMode.FILL) {
                            p.sendMessage("設置ブロック: §a" + setBlockMaterial.toString());
                            int blocks = iMax * jMax * kMax;
                            p.sendMessage("総ブロック数: §a" + blocks + "ブロック(" + String.format("%.1f", ((double) blocks / 64)) + "スタック)");
                        }
                        else if(userData.getMode() == FillMode.FRAME) {
                            p.sendMessage("設置ブロック: §a" + setBlockMaterial.toString());
                            int blocks = 0;
                            for (int i = 0; i < iMax; i++) {
                                for (int j = 0; j < jMax; j++) {
                                    for (int k = 0; k < kMax; k++) {
                                        Block b = world.getBlockAt((int) pos1.getX() + (j * finalXc),
                                                (int) pos1.getY() + (i * finalYc),
                                                (int) pos1.getZ() + (k * finalZc));
                                        if (userData.getMode() == FillMode.FRAME) {
                                            int check = getCheck(b, userData);
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
                        else if(userData.getMode() == FillMode.COPY){
                            if(userData.getRotation() == 0){
                                p.sendMessage("回転: §aなし");
                            }
                            else{
                                p.sendMessage("回転: §a" + userData.getRotation() + "度");
                            }
                            p.sendMessage("コピーモードの際はコピー元のブロックと同じブロックが要求されます");
                        }
                        UUID threadID = UUID.randomUUID();
                        userData.getThread().put(threadID, new org.minecraft.autofill.Process(true));
                        Process process = userData.getThread().get(threadID);
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
                                            if(!p.isOnline()) process.placing = false;
                                            if (process.placing) {
                                                try {
                                                    Block b = world.getBlockAt((int) pos1.getX() + (j * finalXc),
                                                            (int) pos1.getY() + (i * finalYc),
                                                            (int) pos1.getZ() + (k * finalZc));
                                                    if(!userData.isPlacingBlock(b.hashCode())) {
                                                        userData.addFlag(b.hashCode());
                                                        if (userData.getMode() == FillMode.COPY) {
                                                            layer3.add(b.getBlockData().clone());
                                                            continue;
                                                        }
                                                        if (userData.getMode() == FillMode.FRAME) {
                                                            int check = getCheck(b, userData);
                                                            if (check < 2) {
                                                                continue;
                                                            }
                                                        }
                                                        boolean canBuild = canBuilt(regions, b.getLocation(), p);
                                                        if (!Config.checkReplaceableBlocks(b.getBlockData().getMaterial()) && canBuild) {
                                                            if (p.hasPermission("mofucraft.staff") || userData.takeItem(p, setBlockMaterial)) {
                                                                Location bLoc = b.getLocation();
                                                                if (CoreProtectAPI.getAPI() != null) {
                                                                    CoreProtectAPI.getAPI().logPlacement(p.getName(), b.getLocation(), setBlockMaterial, null);
                                                                }
                                                                ;
                                                                setUnnaturalBlock(b);
                                                                setType(b, setBlock);
                                                                if (Config.getJobsBlockTimer() != 0) {
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
                                                        }
                                                        userData.removeFlag(b.hashCode());
                                                    }
                                                } catch (InterruptedException e) {
                                                    System.out.println(e.getMessage());
                                                }
                                            }
                                            if (!process.placing) break;
                                        }
                                        if(userData.getMode() == FillMode.COPY){
                                            layer2.add(layer3);
                                        }
                                        if (!process.placing) break;
                                    }
                                    if(userData.getMode() == FillMode.COPY){
                                        layer1.add(layer2);
                                    }
                                    if (!process.placing) break;
                                }
                                if(userData.getMode() == FillMode.COPY){
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
                                                if(!p.isOnline()) process.placing = false;
                                                if (process.placing) {
                                                    try {
                                                        Block b;
                                                        if(userData.getRotation() == 0){
                                                            b = world.getBlockAt((int) copyPos.getX() + (j * finalXc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (k * finalZc));
                                                        }
                                                        else if(userData.getRotation() == 90){
                                                            b = world.getBlockAt((int) copyPos.getX() + (k * finalZc),
                                                                    (int) copyPos.getY() + (i * finalYc),
                                                                    (int) copyPos.getZ() + (j * finalXc));
                                                            copyBlock.rotate(StructureRotation.CLOCKWISE_90);
                                                        }
                                                        else if(userData.getRotation() == 180){
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
                                                            if(EconomyAPI.getAPI().getBalance(p) < Config.getCopyCost()){
                                                                p.sendMessage("§8[§6AutoFill§8] §c所持金が不足しているためautofillを終了します");
                                                                process.placing = false;
                                                                continue;
                                                            }
                                                            if (p.hasPermission("mofucraft.staff") || userData.takeItem(p,copyMaterial)) {
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
                                                        }
                                                    } catch (InterruptedException e) {
                                                        System.out.println(e.getMessage());
                                                    }
                                                }
                                                if (!process.placing) break;
                                            }
                                            if (!process.placing) break;
                                        }
                                        if (!process.placing) break;
                                    }
                                }
                                if (process.placing) {
                                    p.sendMessage("§8[§6AutoFill§8] §aautofillが完了しました");
                                }
                                userData.getThread().remove(threadID);
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
    public List<String> tabCompleterProcess(CommandSender commandSender, org.bukkit.command.Command command, String label, String[] args) {
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

    private static int getCheck(Block b, UserData userData) {
        int check = 0;
        if (b.getX() == (int) userData.getFirstPosition().getX() || b.getX() == (int) userData.getSecondPosition().getX()) {
            check++;
        }
        if (b.getY() == (int) userData.getFirstPosition().getY() || b.getY() == (int) userData.getSecondPosition().getY()) {
            check++;
        }
        if (b.getZ() == (int) userData.getFirstPosition().getZ() || b.getZ() == (int) userData.getSecondPosition().getZ()) {
            check++;
        }
        return check;
    }
}
