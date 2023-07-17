package org.minecraft.autofill;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class FillData {
    public Material blockData = null;
    public Location position1 = null;
    public Location position2 = null;
    public boolean placing = false;
    public FillData(){

    }
    public boolean canBlockFill(Player P){
        if(blockData != null && blockData.isBlock()){
            if(position1 != null){
                if(position2 != null){
                    return true;
                }
            }
        }
        return false;
    }
}
