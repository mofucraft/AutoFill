package org.minecraft.autofill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

public class UserData {
    private BlockData blockData;
    private Location firstPosition;
    private Location secondPosition;
    private Location copyPosition;
    private SelectMode selectMode;
    private FillMode mode;
    private int rotation;
    private Map<UUID,Process> thread;
    private final HashSet<Integer> fillingBlockHashCode;
    private final Set<Integer> synchronizedFillingBlockHashCode;

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

    public int getRotation() {
        return rotation;
    }
    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public Map<UUID, Process> getThread() {
        return thread;
    }
    public void setThread(Map<UUID, Process> thread) {
        this.thread = thread;
    }

    public UserData(){
        this.blockData = null;
        this.firstPosition = null;
        this.secondPosition = null;
        this.copyPosition = null;
        this.selectMode = SelectMode.NORMAL;
        this.mode = FillMode.FILL;
        this.rotation = 0;
        this.thread = new HashMap<>();
        this.fillingBlockHashCode = new HashSet<>();
        this.synchronizedFillingBlockHashCode = Collections.synchronizedSet(this.fillingBlockHashCode);
    }

    public boolean canBlockFill(Player p){
        if(blockData != null && blockData.getMaterial().isBlock()){
            if(firstPosition != null){
                return secondPosition != null;
            }
        }
        return false;
    }

    public void addFlag(int blockHashCode){
        synchronized (synchronizedFillingBlockHashCode) {
            this.synchronizedFillingBlockHashCode.add(blockHashCode);
        }
    }

    public void removeFlag(int blockHashCode){
        synchronized (synchronizedFillingBlockHashCode) {
            this.synchronizedFillingBlockHashCode.remove(blockHashCode);
        }
    }

    public boolean isPlacingBlock(int blockHashCode){
        synchronized (synchronizedFillingBlockHashCode) {
            return this.synchronizedFillingBlockHashCode.contains(blockHashCode);
        }
    }

    public boolean takeItem(Player p, Material material){
        Inventory inv = p.getInventory();
        int slot = inv.first(material);
        if(slot == -1) {
            return false;
        }
        ItemStack item = inv.getItem(slot);
        item.setAmount(item.getAmount() - 1);
        p.getInventory().setItem(slot, item);
        return true;
    }
}
