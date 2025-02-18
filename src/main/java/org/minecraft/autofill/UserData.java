package org.minecraft.autofill;

import database.PlayerStatusDatabase;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.*;

public class UserData {
    private final Player player;
    private BlockData blockData;
    private Location firstPosition;
    private Location secondPosition;
    private Location copyPosition;
    private SelectMode selectMode;
    private FillMode mode;
    private RotationAngle rotationAngle;
    private boolean exporting;
    private boolean importing;
    private ArrayList<ArrayList<ArrayList<BlockData>>> structure;
    private final ArrayList<FillTask> fillTaskList;
    private final HashSet<Integer> fillingBlockHashCode;
    private final Set<Integer> synchronizedFillingBlockHashCode;

    public Player getPlayer(){
        return this.player;
    }

    public BlockData getBlockData() {
        return blockData;
    }
    public void setBlockData(BlockData blockData) {
        this.blockData = blockData;
    }

    public Location getFirstPosition() {
        return firstPosition;
    }
    public void setFirstPosition(Location firstPosition) {
        this.firstPosition = firstPosition;
    }

    public Location getSecondPosition() {
        return secondPosition;
    }
    public void setSecondPosition(Location secondPosition) {
        this.secondPosition = secondPosition;
    }

    public Location getCopyPosition() {
        return copyPosition;
    }
    public void setCopyPosition(Location copyPosition) {
        this.copyPosition = copyPosition;
    }

    public SelectMode getSelectMode() {
        return selectMode;
    }
    public void setSelectMode(SelectMode selectMode) {
        this.selectMode = selectMode;
    }

    public FillMode getMode() {
        return mode;
    }
    public void setMode(FillMode mode) {
        this.mode = mode;
    }

    public RotationAngle getRotationAngle() {
        return rotationAngle;
    }
    public void setRotationAngle(RotationAngle rotationAngle) {
        this.rotationAngle = rotationAngle;
    }

    public boolean isExporting() {
        return exporting;
    }
    public void setExporting(boolean exporting) {
        this.exporting = exporting;
    }

    public boolean isImporting() {
        return importing;
    }
    public void setImporting(boolean importing) {
        this.importing = importing;
    }

    public ArrayList<ArrayList<ArrayList<BlockData>>> getStructure() {
        return structure;
    }
    public void setStructure(ArrayList<ArrayList<ArrayList<BlockData>>> structure) {
        this.structure = structure;
    }

    public ArrayList<FillTask> getFillTaskList(){
        return this.fillTaskList;
    }

    public UserData(Player player){
        this.player = player;
        this.blockData = null;
        this.firstPosition = null;
        this.secondPosition = null;
        this.copyPosition = null;
        this.selectMode = SelectMode.NORMAL;
        this.mode = FillMode.FILL;
        this.rotationAngle = RotationAngle.ANGLE_0;
        this.exporting = false;
        this.importing = false;
        this.structure = null;
        this.fillTaskList = new ArrayList<>();
        this.fillingBlockHashCode = new HashSet<>();
        this.synchronizedFillingBlockHashCode = Collections.synchronizedSet(this.fillingBlockHashCode);
    }

    public void removeFlag(int blockHashCode){
        synchronized (this.synchronizedFillingBlockHashCode) {
            this.synchronizedFillingBlockHashCode.remove(blockHashCode);
        }
    }

    public boolean isCanPlaceBlock(int blockHashCode){
        synchronized (this.synchronizedFillingBlockHashCode) {
            if(!this.synchronizedFillingBlockHashCode.contains(blockHashCode)){
                this.synchronizedFillingBlockHashCode.add(blockHashCode);
                return true;
            }
            return false;
        }
    }

    public boolean takeItem(Player p, BlockData takeBlockData){
        synchronized (p.getInventory()) {
            Inventory inv = p.getInventory();
            int slot = inv.first(takeBlockData.getMaterial());
            if (slot == -1) {
                return false;
            }
            int remove = 1;
            if(takeBlockData instanceof Slab){
                Slab.Type type = ((Slab)takeBlockData).getType();
                if(type.equals(Slab.Type.DOUBLE)){
                    remove = 2;
                }
            }
            ItemStack item = inv.getItem(slot);
            if(remove == 1) {
                if (item.getAmount() - 1 > 0) {
                    item.setAmount(item.getAmount() - 1);
                    inv.setItem(slot, item);
                } else {
                    inv.setItem(slot, null);
                }
            }
            else{
                if (item.getAmount() - remove > 0) {
                    item.setAmount(item.getAmount() - remove);
                    inv.setItem(slot, item);
                    return true;
                } else {
                    remove -= item.getAmount();
                    inv.setItem(slot, null);
                    int secondSlot = inv.first(takeBlockData.getMaterial());
                    if (secondSlot == -1) {
                        inv.setItem(slot, item);
                        return false;
                    }
                    ItemStack secondItem = inv.getItem(secondSlot);
                    secondItem.setAmount(secondItem.getAmount() - remove);
                    inv.setItem(secondSlot, secondItem);
                }
            }
            return true;
        }
    }

    public FillTaskParameter runFillTask() {
        try(PlayerStatusDatabase con = new PlayerStatusDatabase()){
            for(int i = 0;i < this.fillTaskList.size();i++){
                FillTask fillTask = this.fillTaskList.get(i);
                if(!fillTask.isPlacing()){
                    FillTask newFillTask = new FillTask(this, i + 1);
                    FillTaskParameter fillTaskParameter = newFillTask.generateParameter();
                    this.fillTaskList.set(i, newFillTask);
                    newFillTask.start();
                    return fillTaskParameter;
                }
            }
            if(this.fillTaskList.size() < con.getPlayerStatus(this.player).getMaxThread()){
                FillTask newFillTask = new FillTask(this, this.fillTaskList.size() + 1);
                FillTaskParameter fillTaskParameter = newFillTask.generateParameter();
                this.fillTaskList.add(newFillTask);
                newFillTask.start();
                return fillTaskParameter;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public boolean stopFillTask(Integer taskNumber){
        boolean statusChanged = false;
        if(taskNumber == null){
            for(FillTask fillTask:this.fillTaskList){
                if(fillTask.isPlacing()){
                    fillTask.cancel();
                    statusChanged = true;
                }
            }
        }
        else{
            FillTask fillTask = this.fillTaskList.get(taskNumber);
            if(fillTask != null){
                fillTask.cancel();
                statusChanged = true;
            }
        }
        return statusChanged;
    }

    public int getPlacingThread(){
        int count = 0;
        for(FillTask fillTask:this.fillTaskList){
            if(fillTask.isPlacing()){
                count++;
            }
        }
        return count;
    }
}
