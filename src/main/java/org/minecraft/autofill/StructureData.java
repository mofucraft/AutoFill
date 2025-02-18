package org.minecraft.autofill;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class StructureData implements Serializable {
    private final String[] blockDataList;
    private final ArrayList<ArrayList<ArrayList<String>>> structure;

    public StructureData(ArrayList<ArrayList<ArrayList<BlockData>>> structureData){
        LinkedHashMap<String, Integer> blockList = new LinkedHashMap<>();
        ArrayList<ArrayList<ArrayList<String>>> list1 = new ArrayList<>();
        for (ArrayList<ArrayList<BlockData>> structureData2 : structureData) {
            ArrayList<ArrayList<String>> list2 = new ArrayList<>();
            for (ArrayList<BlockData> structureData3 : structureData2) {
                ArrayList<String> list3 = new ArrayList<>();
                for (BlockData blockData : structureData3) {
                    if(blockData.getMaterial().isAir()){
                        list3.add(null);
                        continue;
                    }
                    if(blockList.containsKey(blockData.getAsString())){
                        list3.add(String.valueOf(blockList.get(blockData.getAsString())));
                    }
                    else{
                        list3.add(String.valueOf(blockList.size()));
                        blockList.put(blockData.getAsString(),blockList.size());
                    }
                }
                list2.add(list3);
            }
            list1.add(list2);
        }
        String[] blockDataList = blockList.keySet().toArray(new String[0]);
        for(int i = 0;i<blockDataList.length;i++){
            blockDataList[i] = blockDataList[i].replace("minecraft:","-");
        }
        this.blockDataList = blockDataList;
        this.structure = list1;
    }

    public ArrayList<ArrayList<ArrayList<BlockData>>> getStructure() {
        ArrayList<ArrayList<ArrayList<BlockData>>> list1 = new ArrayList<>();
        String[] blockDataList = this.blockDataList;
        for(int i = 0;i<blockDataList.length;i++){
            blockDataList[i] = blockDataList[i].replace("-","minecraft:");
        }
        for (ArrayList<ArrayList<String>> structureData2 : this.structure) {
            ArrayList<ArrayList<BlockData>> list2 = new ArrayList<>();
            for (ArrayList<String> structureData3 : structureData2) {
                ArrayList<BlockData> list3 = new ArrayList<>();
                for (String block : structureData3) {
                    if(block == null){
                        list3.add(Material.AIR.createBlockData());
                        continue;
                    }
                    list3.add(Bukkit.getServer().createBlockData(blockDataList[Integer.parseInt(block)]));
                }
                list2.add(list3);
            }
            list1.add(list2);
        }
        return list1;
    }
}
