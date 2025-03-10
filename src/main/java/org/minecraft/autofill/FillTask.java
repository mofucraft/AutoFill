package org.minecraft.autofill;

import api.CoreProtectAPI;
import api.EconomyAPI;
import api.WorldGuardAPI;
import com.gamingmesh.jobs.Jobs;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.MetadataConstants;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import common.InventoryUtil;
import common.PluginUtil;
import common.Util;
import config.Config;
import database.PlayerStatusDatabase;
import language.LanguageUtil;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.bukkit.Bukkit.getServer;

public class FillTask extends Thread {
    private final UUID threadId;
    private final int threadNumber;
    private final UserData userData;
    private boolean placing;
    private int progress;
    private boolean canceled;
    private FillTaskParameter fillTaskParameter;

    public UUID getThreadId(){
        return this.threadId;
    }

    public int getThreadNumber(){
        return this.threadNumber;
    }

    public boolean isPlacing(){
        return this.placing;
    }
    public void setPlacing(boolean placing){
        this.placing = placing;
    }

    public int getProgress(){
        return this.progress;
    }

    public FillTaskParameter generateParameter(){
        World world = getServer().getWorld(Config.getAllowWorldName());
        Location firstPosition = null;
        Location secondPosition = null;
        int Xc;
        int Yc;
        int Zc;
        if(this.userData.getStructure() == null){
            firstPosition = new Location(null,
                Math.min(this.userData.getFirstPosition().getX(),
                        this.userData.getSecondPosition().getX()),
                Math.min(this.userData.getFirstPosition().getY(),
                        this.userData.getSecondPosition().getY()),
                Math.min(this.userData.getFirstPosition().getZ(),
                        this.userData.getSecondPosition().getZ()));
            secondPosition = new Location(null,
                    Math.max(this.userData.getFirstPosition().getX(),
                            this.userData.getSecondPosition().getX()),
                    Math.max(this.userData.getFirstPosition().getY(),
                            this.userData.getSecondPosition().getY()),
                    Math.max(this.userData.getFirstPosition().getZ(),
                            this.userData.getSecondPosition().getZ()));
            Xc = (int) secondPosition.getX() - (int) firstPosition.getX();
            Yc = (int) secondPosition.getY() - (int) firstPosition.getY();
            Zc = (int) secondPosition.getZ() - (int) firstPosition.getZ();
        }
        else{
            Xc = this.userData.getStructure().get(0).size() - 1;
            Yc = this.userData.getStructure().size() - 1;
            Zc = this.userData.getStructure().get(0).get(0).size() - 1;
        }
        int jMax = Math.abs(Xc) + 1;
        int iMax = Math.abs(Yc) + 1;
        int kMax = Math.abs(Zc) + 1;
        Location copyPos = this.userData.getCopyPosition();
        BlockData setBlock = this.userData.getBlockData();
        if (Xc != 0) Xc = Xc / Math.abs(Xc);
        else Xc = 1;
        Yc = 1;
        if (Zc != 0) Zc = Zc / Math.abs(Zc);
        else Zc = 1;
        int totalLoopCount = iMax * jMax * kMax;
        int blocks = 0;
        if(this.userData.getMode() == FillMode.FILL) {
            blocks = iMax * jMax * kMax;
        }
        else if(this.userData.getMode() == FillMode.FRAME) {
            for (int i = 0; i < iMax; i++) {
                for (int j = 0; j < jMax; j++) {
                    for (int k = 0; k < kMax; k++) {
                        Block b = world.getBlockAt((int) firstPosition.getX() + (j * Xc),
                                (int) firstPosition.getY() + (i * Yc),
                                (int) firstPosition.getZ() + (k * Zc));
                        if (this.userData.getMode() == FillMode.FRAME) {
                            int check = FillTask.getAxisCrossingCount(b, this.userData);
                            if (check < 2) {
                                continue;
                            }
                            blocks++;
                        }
                    }
                }
            }
        }
        this.fillTaskParameter = new FillTaskParameter(this.threadId,
                this.threadNumber,
                this.userData,
                world,
                setBlock,
                firstPosition,
                secondPosition,
                copyPos,
                this.userData.getMode(),
                this.userData.getRotationAngle(),
                this.userData.getStructure(),
                jMax,
                iMax,
                kMax,
                Xc,
                Yc,
                Zc,
                totalLoopCount,
                blocks);
        return this.fillTaskParameter;
    }

    public FillTask(UserData userData, int threadNumber){
        this.threadId = UUID.randomUUID();
        this.threadNumber = threadNumber;
        this.userData = userData;
        this.placing = true;
        this.progress = 0;
        this.canceled = false;
    }

    @Override
    public void run() {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(this.fillTaskParameter.getWorld()));
        boolean hasAdminPermission = Util.hasValidPermission(this.userData.getPlayer(), Config.getAdminPermission());
        if(this.userData.getMode() == FillMode.FILL || this.userData.getMode() == FillMode.FRAME) {
            for (int i = 0; i < this.fillTaskParameter.getYSize(); i++) {
                for (int j = 0; j < this.fillTaskParameter.getXSize(); j++) {
                    for (int k = 0; k < this.fillTaskParameter.getZSize(); k++) {
                        if (!this.userData.getPlayer().isOnline()) this.placing = false;
                        if (this.placing) {
                            try {
                                Block b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getFirstPosition().getX() + (j * this.fillTaskParameter.getXSide()),
                                        (int) this.fillTaskParameter.getFirstPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                        (int) this.fillTaskParameter.getFirstPosition().getZ() + (k * this.fillTaskParameter.getZSide()));
                                if (this.userData.getMode() == FillMode.FRAME) {
                                    int check = getAxisCrossingCount(b, this.userData);
                                    if (check < 2) {
                                        continue;
                                    }
                                }
                                if (this.userData.isCanPlaceBlock(b.hashCode())) {
                                    if (Config.isReplaceableBlock(b.getBlockData().getMaterial()) &&
                                            (hasAdminPermission ||
                                                    WorldGuardAPI.isCanBuiltProtectedArea(regions, b.getLocation(), this.userData.getPlayer()))) {
                                        if (hasAdminPermission) {
                                            setBlock(this.userData.getPlayer(), b, this.fillTaskParameter.getBlockData());
                                            Thread.sleep(Config.getBlockPlaceCooldown());
                                        } else {
                                            if (InventoryUtil.takeItemSync(this.userData.getPlayer(), this.fillTaskParameter.getBlockData())) {
                                                setBlock(this.userData.getPlayer(), b, this.fillTaskParameter.getBlockData());
                                                Thread.sleep(Config.getBlockPlaceCooldown());
                                            }
                                            else{
                                                try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                                                    LanguageUtil.sendMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(), "notEnoughBlock");
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                this.placing = false;
                                            }
                                        }
                                    }
                                    this.userData.removeFlag(b.hashCode());
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        this.progress = (int)(((double)(i * this.fillTaskParameter.getXSize() * this.fillTaskParameter.getZSize()) + (j * this.fillTaskParameter.getZSize()) + k) / (double)this.fillTaskParameter.getTotalLoopCount() * 100.0);
                        if (!this.placing) break;
                    }
                    if (!this.placing) break;
                }
                if (!this.placing) break;
            }
        }
        else if(this.userData.getMode() == FillMode.COPY && this.userData.getStructure() == null){
            for (int i = 0; i < this.fillTaskParameter.getYSize(); i++) {
                for (int j = 0; j < this.fillTaskParameter.getXSize(); j++) {
                    for (int k = 0; k < this.fillTaskParameter.getZSize(); k++) {
                        BlockData copyBlockData = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getFirstPosition().getX() + (j * this.fillTaskParameter.getXSide()),
                                (int) this.fillTaskParameter.getFirstPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                (int) this.fillTaskParameter.getFirstPosition().getZ() + (k * this.fillTaskParameter.getZSide())).getBlockData().clone();
                        if(copyBlockData.getMaterial().isAir()) {
                            continue;
                        }
                        if(!this.userData.getPlayer().isOnline()) {
                            this.placing = false;
                        }
                        if (this.placing) {
                            try {
                                Block b;
                                if(this.fillTaskParameter.getRotationAngle() == RotationAngle.ANGLE_0){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (j * this.fillTaskParameter.getXSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (k * this.fillTaskParameter.getZSide()));
                                }
                                else if(this.fillTaskParameter.getRotationAngle() == RotationAngle.ANGLE_90){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (this.fillTaskParameter.getZSize() - 1) - (k * this.fillTaskParameter.getZSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (j * this.fillTaskParameter.getXSide()));
                                    copyBlockData.rotate(StructureRotation.CLOCKWISE_90);
                                }
                                else if(this.fillTaskParameter.getRotationAngle() == RotationAngle.ANGLE_180){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (this.fillTaskParameter.getXSize() - 1) - (j * this.fillTaskParameter.getXSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (this.fillTaskParameter.getZSize() - 1) - (k * this.fillTaskParameter.getZSide()));
                                    copyBlockData.rotate(StructureRotation.CLOCKWISE_180);
                                }
                                else{
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (k * this.fillTaskParameter.getZSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (this.fillTaskParameter.getXSize() - 1) - (j * this.fillTaskParameter.getXSide()));
                                    copyBlockData.rotate(StructureRotation.COUNTERCLOCKWISE_90);
                                }
                                if(Config.isDisabledBlock(copyBlockData.getMaterial())){
                                    continue;
                                }
                                if(this.userData.isCanPlaceBlock(b.hashCode())) {
                                    if (Config.isReplaceableBlock(b.getBlockData().getMaterial()) &&
                                            (hasAdminPermission ||
                                                    WorldGuardAPI.isCanBuiltProtectedArea(regions, b.getLocation(), this.userData.getPlayer()))) {
                                        boolean nonConsumableBlock = Config.isNonConsumableBlock(copyBlockData.getMaterial());
                                        if (!nonConsumableBlock && EconomyAPI.getAPI().getBalance(this.userData.getPlayer()) < Config.getCopyCost()) {
                                            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                                                LanguageUtil.sendMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(), "notEnoughMoney");
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                            this.placing = false;
                                            continue;
                                        }
                                        if (nonConsumableBlock || hasAdminPermission) {
                                            setBlock(this.userData.getPlayer(), b, copyBlockData);
                                            Thread.sleep(Config.getBlockPlaceCooldown());
                                        } else {
                                            if (InventoryUtil.takeItemSync(this.userData.getPlayer(), copyBlockData)) {
                                                setBlock(this.userData.getPlayer(), b, copyBlockData);
                                                Thread.sleep(Config.getBlockPlaceCooldown());
                                            } else {
                                                try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                                                    Map<String, String> variables = new HashMap<>();
                                                    variables.put("materialName", copyBlockData.getMaterial().toString());
                                                    LanguageUtil.sendReplacedMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(), "notEnoughCopyBlock", variables);
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                this.placing = false;
                                            }
                                        }
                                    }
                                    this.userData.removeFlag(b.hashCode());
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        this.progress = (int)(((double)(i * this.fillTaskParameter.getXSize() * this.fillTaskParameter.getZSize()) + (j * this.fillTaskParameter.getZSize()) + k) / (double)this.fillTaskParameter.getTotalLoopCount() * 100.0);
                        if (!this.placing) break;
                    }
                    if (!this.placing) break;
                }
                if (!this.placing) break;
            }
        }
        else if(this.userData.getMode() == FillMode.COPY && this.userData.getStructure() != null){
            for (int i = 0; i < this.fillTaskParameter.getStructure().size(); i++) {
                for (int j = 0; j < this.fillTaskParameter.getStructure().get(0).size(); j++) {
                    for (int k = 0; k < this.fillTaskParameter.getStructure().get(0).get(0).size(); k++) {
                        BlockData copyBlockData = this.fillTaskParameter.getStructure()
                                .get(i * this.fillTaskParameter.getYSide())
                                .get(j * this.fillTaskParameter.getXSide())
                                .get(k * this.fillTaskParameter.getZSide()).clone();
                        if(copyBlockData.getMaterial().isAir()) {
                            continue;
                        }
                        if(!this.userData.getPlayer().isOnline()) {
                            this.placing = false;
                        }
                        if (this.placing) {
                            try {
                                Block b;
                                if(this.fillTaskParameter.getRotationAngle() == RotationAngle.ANGLE_0){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (j * this.fillTaskParameter.getXSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (k * this.fillTaskParameter.getZSide()));
                                }
                                else if(this.fillTaskParameter.getRotationAngle()== RotationAngle.ANGLE_90){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (this.fillTaskParameter.getZSize() - 1) - (k * this.fillTaskParameter.getZSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (j * this.fillTaskParameter.getXSide()));
                                    copyBlockData.rotate(StructureRotation.CLOCKWISE_90);
                                }
                                else if(this.fillTaskParameter.getRotationAngle() == RotationAngle.ANGLE_180){
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (this.fillTaskParameter.getXSize() - 1) - (j * this.fillTaskParameter.getXSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (this.fillTaskParameter.getZSize() - 1) - (k * this.fillTaskParameter.getZSide()));
                                    copyBlockData.rotate(StructureRotation.CLOCKWISE_180);
                                }
                                else{
                                    b = this.fillTaskParameter.getWorld().getBlockAt((int) this.fillTaskParameter.getCopyPosition().getX() + (k * this.fillTaskParameter.getZSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getY() + (i * this.fillTaskParameter.getYSide()),
                                            (int) this.fillTaskParameter.getCopyPosition().getZ() + (this.fillTaskParameter.getXSize() - 1) - (j * this.fillTaskParameter.getXSide()));
                                    copyBlockData.rotate(StructureRotation.COUNTERCLOCKWISE_90);
                                }
                                if(Config.isDisabledBlock(copyBlockData.getMaterial())){
                                    continue;
                                }
                                if(this.userData.isCanPlaceBlock(b.hashCode())) {
                                    if (Config.isReplaceableBlock(b.getBlockData().getMaterial()) &&
                                            (hasAdminPermission ||
                                                    WorldGuardAPI.isCanBuiltProtectedArea(regions, b.getLocation(), this.userData.getPlayer()))) {
                                        boolean nonConsumableBlock = Config.isNonConsumableBlock(copyBlockData.getMaterial());
                                        if (!nonConsumableBlock && EconomyAPI.getAPI().getBalance(this.userData.getPlayer()) < Config.getCopyCost()) {
                                            try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                                                LanguageUtil.sendMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(), "notEnoughMoney");
                                            } catch (SQLException e) {
                                                throw new RuntimeException(e);
                                            }
                                            this.placing = false;
                                            continue;
                                        }
                                        if (nonConsumableBlock || hasAdminPermission) {
                                            setBlock(this.userData.getPlayer(), b, copyBlockData);
                                            Thread.sleep(Config.getBlockPlaceCooldown());
                                        } else {
                                            if (InventoryUtil.takeItemSync(this.userData.getPlayer(), copyBlockData)) {
                                                setBlock(this.userData.getPlayer(), b, copyBlockData);
                                                Thread.sleep(Config.getBlockPlaceCooldown());
                                            } else {
                                                try (PlayerStatusDatabase database = new PlayerStatusDatabase()) {
                                                    Map<String, String> variables = new HashMap<>();
                                                    variables.put("materialName", copyBlockData.getMaterial().toString());
                                                    LanguageUtil.sendReplacedMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(), "notEnoughCopyBlock", variables);
                                                } catch (SQLException e) {
                                                    throw new RuntimeException(e);
                                                }
                                                this.placing = false;
                                            }
                                        }
                                    }
                                    this.userData.removeFlag(b.hashCode());
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        this.progress = (int)(((double)(i * this.fillTaskParameter.getXSize() * this.fillTaskParameter.getZSize()) + (j * this.fillTaskParameter.getZSize()) + k) / (double)this.fillTaskParameter.getTotalLoopCount() * 100.0);
                        if (!this.placing) break;
                    }
                    if (!this.placing) break;
                }
                if (!this.placing) break;
            }
        }
        this.progress = 100;
        if (this.placing && !canceled) {
            try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
                Map<String, String> variables = new HashMap<>();
                variables.put("threadName","Thread" + String.format("%02d", this.threadNumber));
                LanguageUtil.sendReplacedMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(),"completionAutoFill", variables);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            this.placing = false;
        }
    }

    public boolean cancel(){
        this.placing = false;
        this.canceled = true;
        try(PlayerStatusDatabase database = new PlayerStatusDatabase()){
            Map<String, String> variables = new HashMap<>();
            variables.put("threadName","Thread" + String.format("%02d", this.threadNumber));
            LanguageUtil.sendReplacedMessage(this.userData.getPlayer(), database.getPlayerStatus(this.userData.getPlayer()).getUsingLanguage(),"cancelAutoFill", variables);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public static int getAxisCrossingCount(Block b, UserData userData) {
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

    private static void setBlock(Player p, Block b, BlockData setBlock){
        Location bLoc = b.getLocation();
        if (CoreProtectAPI.getAPI() != null) {
            CoreProtectAPI.getAPI().logPlacement(p.getName(), b.getLocation(), setBlock.getMaterial(), setBlock);
        }
        setUnnaturalBlock(b);
        setType(b, setBlock);
        if (Config.getJobsDisableTime() != 0) {
            Jobs.getBpManager().add(b, Config.getJobsDisableTime());
        }
        getServer().getOnlinePlayers().forEach(player -> {
            player.playSound(bLoc, setBlock.getSoundGroup().getPlaceSound(), 1, 1);
        });
    }

    private static void setType(final Block b, final BlockData bd){
        new BukkitRunnable() {
            public void run() {
                b.setBlockData(bd);
                b.getState().update(true, true);
            }
        }.runTask(PluginUtil.getPlugin());
    }

    private static void setUnnaturalBlock(@NotNull Block block) {
        mcMMO.getPlaceStore().setTrue(block);

        // Failsafe against lingering metadata
        if(block.hasMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS))
            block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, mcMMO.p);
    }
}
