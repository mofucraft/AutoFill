package org.minecraft.autofill;

public enum RotationAngle {
    ANGLE_0(0),
    ANGLE_90(90),
    ANGLE_180(180),
    ANGLE_270(270);
    private final int angle;

    RotationAngle(final int angle) {
        this.angle = angle;
    }

    public int getAngle(){
        return this.angle;
    }

    public String toString() {
        return Integer.toString(this.angle);
    }
}
