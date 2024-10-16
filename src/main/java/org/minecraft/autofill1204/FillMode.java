package org.minecraft.autofill1204;

public enum FillMode {
    Fill("Fill"),
    Frame("Frame"),
    Copy("Copy");
    private final String text;

    FillMode(final String text) {
        this.text = text;
    }

    public String getString() {
        return this.text;
    }
}
