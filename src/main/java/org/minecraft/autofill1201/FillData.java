package org.minecraft.autofill1201;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FillData {
    public BlockData blockData = null;
    public Location position1 = null;
    public Location position2 = null;
    public Location copyPosition = null;
    public int rotation = 0;
    public Map<UUID,Process> thread = new HashMap<>();
    public FillMode Mode = FillMode.Fill;
    public SelectMode selectMode = SelectMode.Normal;
    public FillData(){

    }
    public boolean canBlockFill(Player P){
        if(blockData != null && blockData.getMaterial().isBlock()){
            if(position1 != null){
                if(position2 != null){
                    return true;
                }
            }
        }
        return false;
    }
}
