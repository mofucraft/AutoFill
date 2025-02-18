package org.minecraft.autofill;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;

import java.util.ArrayList;
import java.util.UUID;

public class FillTaskParameter {
    private final UUID threadId;
    private final int threadNumber;
    private final UserData userData;
    private final World world;
    private final BlockData blockData;
    private final Location firstPosition;
    private final Location secondPosition;
    private final Location copyPosition;
    private final FillMode fillMode;
    private final RotationAngle rotationAngle;
    private final ArrayList<ArrayList<ArrayList<BlockData>>> structure;
    private final int xSize;
    private final int ySize;
    private final int zSize;
    private final int xSide;
    private final int ySide;
    private final int zSide;
    private final long totalLoopCount;
    private final long totalBlockCount;

    public FillTaskParameter(UUID threadId, int threadNumber, UserData userData, World world, BlockData blockData, Location firstPosition, Location secondPosition, Location copyPosition, FillMode fillMode, RotationAngle rotationAngle, ArrayList<ArrayList<ArrayList<BlockData>>> structure, int xSize, int ySize, int zSize, int xSide, int ySide, int zSide, long totalLoopCount, long totalBlockCount) {
        this.threadId = threadId;
        this.threadNumber = threadNumber;
        this.userData = userData;
        this.world = world;
        this.blockData = blockData;
        this.firstPosition = firstPosition;
        this.secondPosition = secondPosition;
        this.copyPosition = copyPosition;
        this.fillMode = fillMode;
        this.rotationAngle = rotationAngle;
        this.structure = structure;
        this.xSize = xSize;
        this.ySize = ySize;
        this.zSize = zSize;
        this.xSide = xSide;
        this.ySide = ySide;
        this.zSide = zSide;
        this.totalLoopCount = totalLoopCount;
        this.totalBlockCount = totalBlockCount;
    }

    public UUID getThreadId() {
        return threadId;
    }

    public int getThreadNumber() {
        return threadNumber;
    }

    public UserData getUserData() {
        return userData;
    }

    public World getWorld() {
        return world;
    }

    public BlockData getBlockData() {
        return blockData;
    }

    public Location getFirstPosition() {
        return firstPosition;
    }

    public Location getSecondPosition() {
        return secondPosition;
    }

    public Location getCopyPosition() {
        return copyPosition;
    }

    public FillMode getFillMode() {
        return fillMode;
    }

    public RotationAngle getRotationAngle() {
        return rotationAngle;
    }

    public ArrayList<ArrayList<ArrayList<BlockData>>> getStructure() {
        return structure;
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public int getZSize() {
        return zSize;
    }

    public int getXSide() {
        return xSide;
    }

    public int getYSide() {
        return ySide;
    }

    public int getZSide() {
        return zSide;
    }

    public long getTotalLoopCount() {
        return totalLoopCount;
    }

    public long getTotalBlockCount() {
        return totalBlockCount;
    }
}
