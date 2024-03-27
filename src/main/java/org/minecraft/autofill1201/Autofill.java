package org.minecraft.autofill1201;

import com.gamingmesh.jobs.Jobs;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.util.MetadataConstants;
import org.antlr.v4.runtime.misc.NotNull;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.structure.StructureRotation;
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
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.milkbowl.vault.economy.Economy;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Autofill extends JavaPlugin implements Listener {

    public Map<UUID,FillData> playerData = new Hashtable<>();
    public ArrayList<String> disableBlocks = new ArrayList<>();
    public ArrayList<String> replaceableBlocks = new ArrayList<>();
    public int jobsBlockTimer = 0;
    public double copyCost = 0.0;
    public Material wand = Material.NETHER_STAR;

    public CoreProtectAPI cApi;
    public WorldGuard wApi;
    public Jobs jobs;
    public Economy economy;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        this.getCommand("autofill").setTabCompleter(new TabCompleter());
        cApi = getCoreProtect();
        wApi = WorldGuard.getInstance();
        jobs = Jobs.getInstance();
        setupEconomy();
        loadConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @EventHandler
    public void onClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(e.hasItem() && e.getItem().getType() == wand){
            if(!playerData.containsKey(p.getUniqueId())){
                playerData.put(p.getUniqueId(),new FillData());
            }
            FillData fillData = playerData.get(p.getUniqueId());
            if(fillData.selectMode == SelectMode.Normal) {
                if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第1ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    p.sendMessage("選択中のブロック: §a" + e.getClickedBlock().getBlockData().getMaterial().toString());
                    fillData.position1 = e.getClickedBlock().getLocation();
                    fillData.blockData = e.getClickedBlock().getBlockData().clone();
                } else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("第2ポジションを設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)。範囲選択後/autofillコマンドで一括設置できます");
                    fillData.position2 = e.getClickedBlock().getLocation();
                }
            }
            else if(fillData.selectMode == SelectMode.Copy){
                if (e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("コピー先始点を設定しました(§aX:" + (int) e.getClickedBlock().getLocation().getX() +
                            ",Y:" + (int) e.getClickedBlock().getLocation().getY() +
                            ",Z:" + (int) e.getClickedBlock().getLocation().getZ() + "§f)");
                    p.sendMessage("始点は範囲選択されている領域の座標が小さい方からコピーされます");
                    p.sendMessage("既に範囲選択がされている場合は範囲が表示されます");
                    p.sendMessage("/autofillコマンドを使用するとコピーが開始されます");
                    fillData.copyPosition = e.getClickedBlock().getLocation();
                    fillData.selectMode = SelectMode.Normal;
                    ShowRangeParticle(p);
                }
            }
            playerData.put(p.getUniqueId(),fillData);
            e.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player p = getServer().getPlayer(sender.getName());
        if(args.length == 0) {
            if (command.getName().equalsIgnoreCase("autofill")) {
                if (p.getWorld().getName().equalsIgnoreCase("world")) {
                    World world = getServer().getWorld("world");
                    if (playerData.containsKey(p.getUniqueId())) {
                        FillData fillData = playerData.get(p.getUniqueId());
                        if(fillData.position1 == null || fillData.position2 == null){
                            p.sendMessage("§8[§6AutoFill§8] §c選択ツール(" + wand.toString() + ")で範囲を選択してください");
                            return false;
                        }
                        if(!((fillData.copyPosition == null || fillData.position1.getWorld().getName().equalsIgnoreCase("world")) &&
                                (fillData.copyPosition == null || fillData.position2.getWorld().getName().equalsIgnoreCase("world")) &&
                                (fillData.copyPosition == null || fillData.copyPosition.getWorld().getName().equalsIgnoreCase("world")))){
                            p.sendMessage("§8[§6AutoFill§8] §cautofillは建築ワールドでしか使用できません");
                            return false;
                        }
                        if(checkDisabledBlocks(fillData.blockData.getMaterial())) {
                            if(fillData.canBlockFill(p)) {
                                if(fillData.Mode == FillMode.Copy){
                                    if(fillData.copyPosition == null){
                                        p.sendMessage("§8[§6AutoFill§8] §cCopyモードの際はコピー先を/autofill csetを実行後に始点を設定アイテム(" + wand.toString() + ")でクリックして設定してください");
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
                                fillData.thread.put(threadID, new Process(true));
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
                                                            if (!checkReplaceableBlocks(b.getBlockData().getMaterial()) && canBuild ) {
                                                                Inventory inv = p.getInventory();
                                                                if (inv.contains(setBlockMaterial) || p.hasPermission("mofucraft.staff")) {
                                                                    if(!p.hasPermission("mofucraft.staff")) {
                                                                        int slot = inv.first(setBlockMaterial);
                                                                        ItemStack item = inv.getItem(slot);
                                                                        item.setAmount(item.getAmount() - 1);
                                                                        p.getInventory().setItem(slot, item);
                                                                    }
                                                                    Location bLoc = b.getLocation();
                                                                    if (cApi != null) {
                                                                        cApi.logPlacement(p.getName(), b.getLocation(), setBlockMaterial, null);
                                                                    }
                                                                    setUnnaturalBlock(b);
                                                                    setType(b, setBlock);
                                                                    if(jobsBlockTimer != 0) {
                                                                        Jobs.getBpManager().add(b, jobsBlockTimer);
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
                                                        if(!checkReplaceableBlocks(copyBlock.getMaterial())){
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
                                                                    b = world.getBlockAt((int) copyPos.getX() + (kMax - 1) - (k * finalZc),
                                                                            (int) copyPos.getY() + (i * finalYc),
                                                                            (int) copyPos.getZ() + (j * finalXc));
                                                                    copyBlock.rotate(StructureRotation.CLOCKWISE_90);
                                                                }
                                                                else if(fillData.rotation == 180){
                                                                    b = world.getBlockAt((int) copyPos.getX() + (j * finalXc),
                                                                            (int) copyPos.getY() + (i * finalYc),
                                                                            (int) copyPos.getZ() + (kMax - 1) - (k * finalZc));
                                                                    copyBlock.rotate(StructureRotation.CLOCKWISE_180);
                                                                }
                                                                else{
                                                                    b = world.getBlockAt((int) copyPos.getX() + (k * finalZc),
                                                                            (int) copyPos.getY() + (i * finalYc),
                                                                            (int) copyPos.getZ() + (j * finalXc));
                                                                    copyBlock.rotate(StructureRotation.COUNTERCLOCKWISE_90);
                                                                }
                                                                Sound copyBlockSound = copyBlock.getMaterial().createBlockData().getSoundGroup().getPlaceSound();
                                                                boolean canBuild = canBuilt(regions, b.getLocation(), p);
                                                                if (!checkReplaceableBlocks(b.getBlockData().getMaterial()) && canBuild ) {
                                                                    Inventory inv = p.getInventory();
                                                                    if(economy.getBalance(p) < copyCost){
                                                                        p.sendMessage("§8[§6AutoFill§8] §c所持金が不足しているためautofillを終了します");
                                                                        process.placing = false;
                                                                        continue;
                                                                    }
                                                                    if (inv.contains(copyBlock.getMaterial()) || p.hasPermission("mofucraft.staff")) {
                                                                        if(!p.hasPermission("mofucraft.staff")) {
                                                                            int slot = inv.first(copyBlock.getMaterial());
                                                                            ItemStack item = inv.getItem(slot);
                                                                            item.setAmount(item.getAmount() - 1);
                                                                            p.getInventory().setItem(slot, item);
                                                                        }
                                                                        Location bLoc = b.getLocation();
                                                                        if (cApi != null) {
                                                                            cApi.logPlacement(p.getName(), b.getLocation(), copyBlock.getMaterial(), null);
                                                                        }
                                                                        setUnnaturalBlock(b);
                                                                        setType(b, copyBlock);
                                                                        if(jobsBlockTimer != 0) {
                                                                            Jobs.getBpManager().add(b, jobsBlockTimer);
                                                                        }
                                                                        economy.withdrawPlayer(p,copyCost);
                                                                        getServer().getOnlinePlayers().forEach(player -> {
                                                                            player.playSound(bLoc, copyBlockSound, 1, 1);
                                                                        });
                                                                        Thread.sleep(50);
                                                                    } else {
                                                                        p.sendMessage("§8[§6AutoFill§8] §cコピー元のブロック(" + copyBlock.getMaterial().toString() + ")がインベントリに存在しないためautofillを終了します");
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
                        CheckUserData(p);
                        p.sendMessage("§8[§6AutoFill§8] §c選択ツール(" + wand.toString() + ")で範囲を選択してください");
                    }
                } else {
                    p.sendMessage("§8[§6AutoFill§8] §cautofillは建築ワールドでしか使用できません");
                }
            }
            if (command.getName().equalsIgnoreCase("cancelfill")) {
                CheckUserData(p);
                boolean stop = false;
                FillData fillData = playerData.get(p.getUniqueId());
                if (fillData.thread.size() > 0) stop = true;
                fillData.thread.forEach((uuid, process) -> {
                    process.placing = false;
                });
                if (stop == true) {
                    p.sendMessage("autofillをキャンセルしました");
                }
            }
        }
        else{
            if (args[0].equalsIgnoreCase("reload")) {
                if(p.hasPermission("autofill.reload")) {
                    loadConfig();
                    p.sendMessage("§7---------------§8[§6AutoFill§8]§7---------------");
                    p.sendMessage("autofillコンフィグがリロードされました");
                    p.sendMessage("マテリアルが一つも一致しないものは赤く表示されます");
                    String disableLists = "";
                    String replaceableLists = "";
                    for (String str : disableBlocks) {
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
                    for (String str : replaceableBlocks) {
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
                    p.sendMessage("Jobs無効時間: " + jobsBlockTimer + "秒");
                    p.sendMessage("Copyモードコスト(1ブロック当たり): " + copyCost + "MOFU");
                }
            }
            else if(args[0].equalsIgnoreCase("mode")){
                CheckUserData(p);
                FillData fillData = playerData.get(p.getUniqueId());
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
                            p.sendMessage("§8[§6AutoFill§8] §fCopyモードではコピーするブロック1つにつきそのコピー元のブロックと" + copyCost + "MOFUが必要となります");
                            p.sendMessage("§8[§6AutoFill§8] §f範囲指定後、/autofill csetでコピー先始点を指定してください");
                        }
                    }
                    else{
                        String modeList = "";
                        boolean first = false;
                        for(FillMode modes:FillMode.values()){
                            if(first == false) {
                                modeList += modes.getString();
                                first = true;
                            }
                            else{
                                modeList += "," + modes.getString();
                            }
                        }
                        p.sendMessage("§8[§6AutoFill§8] §f現在設定可能なモードは§a" + modeList + "§fです");
                    }
                    playerData.put(p.getUniqueId(),fillData);
                } else {
                    p.sendMessage("§8[§6AutoFill§8] §f現在は§a" + fillData.Mode + "モード§fに設定されています");
                }
            }
            else if(args[0].equalsIgnoreCase("cset")){
                CheckUserData(p);
                FillData fillData = playerData.get(p.getUniqueId());
                p.sendMessage("§8[§6AutoFill§8] §fコピー先始点を選択ツール(" + wand.toString() + ")で設定してください");
                fillData.selectMode = SelectMode.Copy;
                playerData.put(p.getUniqueId(),fillData);
            }
            else if(args[0].equalsIgnoreCase("rotation")){
                CheckUserData(p);
                FillData fillData = playerData.get(p.getUniqueId());
                if (args.length > 1) {
                    boolean setCheck = true;
                    if(args[1].equalsIgnoreCase("0")){
                        p.sendMessage("§8[§6AutoFill§8] §f回転方向を§aなし§fに設定しました");
                        fillData.rotation = 0;
                    }
                    else if(args[1].equalsIgnoreCase("90")){
                        p.sendMessage("§8[§6AutoFill§8] §f回転方向を§a90度§fに設定しました");
                        fillData.rotation = 90;
                    }
                    else if(args[1].equalsIgnoreCase("180")){
                        p.sendMessage("§8[§6AutoFill§8] §f回転方向を§a180度§fに設定しました");
                        fillData.rotation = 180;
                    }
                    else if(args[1].equalsIgnoreCase("270")){
                        p.sendMessage("§8[§6AutoFill§8] §f回転方向を§a90度§fに設定しました");
                        fillData.rotation = 270;
                    }
                    else{
                        p.sendMessage("§8[§6AutoFill§8] §f回転方向は§a0,90,180,270§fの中から選択できます");
                        setCheck = false;
                    }
                    if(setCheck == true){
                        p.sendMessage("§8[§6AutoFill§8] §f再度コピー開始地点を確認してください");
                        p.sendMessage("§8[§6AutoFill§8] §fまた、回転方向設定はコピーモード時のみ適用されます");
                        ShowRangeParticle(p);
                    }
                    playerData.put(p.getUniqueId(),fillData);
                } else {
                    if(fillData.rotation == 0){
                        p.sendMessage("§8[§6AutoFill§8] §f現在、回転方向は設定されていません");
                    }
                    else{
                        p.sendMessage("§8[§6AutoFill§8] §f現在、回転方向は§a" + fillData.rotation + "度§fに設定されています");
                    }
                }
            }
        }
        return false;
    }

    public void setType(final Block b, final BlockData bd){
        new BukkitRunnable() {
            public void run() {
                b.setBlockData(bd);
                b.getState().update(true, true);
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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
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
                List strList = new ArrayList<String>();
                strList.add("autoFill禁止ブロック設定");
                config.setInlineComments("DISABLE_BLOCKS", strList);
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    java.lang.System.out.println(e.getMessage());
                }
            }
            disableBlocks.clear();
            for (String str:config.getStringList("DISABLE_BLOCKS")) {
                disableBlocks.add(str);
            }
            if(config.getStringList("REPLACEABLE_BLOCKS").isEmpty()){
                config.createSection("REPLACEABLE_BLOCKS");
                List<String> defaultList = new ArrayList<>();
                defaultList.add("AIR");
                config.set("REPLACEABLE_BLOCKS", defaultList);
                List strList = new ArrayList<String>();
                strList.add("置換可能ブロック設定(リスト) ※置き換えられたブロックは消失します");
                config.setInlineComments("REPLACEABLE_BLOCKS", strList);
                try {
                    config.save(configFile);
                } catch (IOException e) {
                    java.lang.System.out.println(e.getMessage());
                }
            }
            replaceableBlocks.clear();
            for (String str:config.getStringList("REPLACEABLE_BLOCKS")) {
                replaceableBlocks.add(str);
            }
            jobsBlockTimer = config.getInt("JOBS_BLOCK_TIMER");
            if(config.getInt("JOBS_BLOCK_TIMER") == 0){
                config.set("JOBS_BLOCK_TIMER", 0);
                List strList = new ArrayList<String>();
                strList.add("Jobsプラグイン無効時間設定(秒)");
                config.setInlineComments("JOBS_BLOCK_TIMER", strList);
                config.save(configFile);
            }
            copyCost = config.getDouble("COPY_COST");
            if(config.getInt("COPY_COST") == 0){
                config.set("COPY_COST", 0);
                List strList = new ArrayList<String>();
                strList.add("Copyモードの1ブロック当たりのコスト(MOFU) ※置換可能ブロックはコスト0");
                config.setInlineComments("COPY_COST", strList);
                config.save(configFile);
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
    private boolean checkReplaceableBlocks(Material material){
        for (String str: replaceableBlocks) {
            if(material.toString().equalsIgnoreCase(str) || material.toString().matches(str)) {
                return false;
            }
        }
        return true;
    }

    public static void setUnnaturalBlock(@NotNull Block block) {
        mcMMO.getPlaceStore().setTrue(block);

        // Failsafe against lingering metadata
        if(block.hasMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS))
            block.removeMetadata(MetadataConstants.METADATA_KEY_BONUS_DROPS, mcMMO.p);
    }
    private void CheckUserData(Player p){
        if(!playerData.containsKey(p.getUniqueId())){
            playerData.put(p.getUniqueId(),new FillData());
        }
    }
    private void ShowRangeParticle(Player p){
        CheckUserData(p);
        FillData fillData = playerData.get(p.getUniqueId());
        if(fillData.position1 != null && fillData.position2 != null) {
            Location pos1;
            Location pos2;
            if(fillData.rotation == 0 || fillData.rotation == 180){
                pos1 = new Location(null,Math.min(fillData.position1.getX(),fillData.position2.getX()),Math.min(fillData.position1.getY(),fillData.position2.getY()),Math.min(fillData.position1.getZ(),fillData.position2.getZ()));
                pos2 = new Location(null,Math.max(fillData.position1.getX(),fillData.position2.getX()),Math.max(fillData.position1.getY(),fillData.position2.getY()),Math.max(fillData.position1.getZ(),fillData.position2.getZ()));
            }
            else{
                pos1 = new Location(null,Math.min(fillData.position1.getZ(),fillData.position2.getZ()),Math.min(fillData.position1.getY(),fillData.position2.getY()),Math.min(fillData.position1.getX(),fillData.position2.getX()));
                pos2 = new Location(null,Math.max(fillData.position1.getZ(),fillData.position2.getZ()),Math.max(fillData.position1.getY(),fillData.position2.getY()),Math.max(fillData.position1.getX(),fillData.position2.getX()));
            }
            int Ycm = (int) pos2.getY() - (int) pos1.getY() + 1;
            int Xcm = (int) pos2.getX() - (int) pos1.getX() + 1;
            int Zcm = (int) pos2.getZ() - (int) pos1.getZ() + 1;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for(int i = 0;i<5;i++){
                        for(int j = 0;j<4;j++) {
                            double Yl = 0.0;
                            double Xl = 0.0;
                            double Zl = 0.0;
                            while (true) {
                                if (j == 0) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(0, Yl, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 1) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xcm, Yl, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 2) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(0, Yl, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 3) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xcm, Yl, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                }
                                Yl += 0.1;
                                if (Yl > Ycm) break;
                            }
                            while (true) {
                                if (j == 0) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xl, 0, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 1) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xl, Ycm, 0), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 2) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xl, 0, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 3) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xl, Ycm, Zcm), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                }
                                Xl += 0.1;
                                if (Xl > Xcm) break;
                            }
                            while (true) {
                                if (j == 0) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(0, 0, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 1) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xcm, 0, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 2) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(0, Ycm, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                } else if (j == 3) {
                                    p.getWorld().spawnParticle(Particle.DUST_COLOR_TRANSITION, fillData.copyPosition.clone().add(Xcm, Ycm, Zl), 0, 0.001, 0, 0, 0, new Particle.DustTransition(Color.RED, Color.RED, 1));
                                }
                                Zl += 0.1;
                                if (Zl > Zcm) break;
                            }
                        }
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }).start();
        }
    }
}
