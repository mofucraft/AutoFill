package org.minecraft.autofill;

public enum FillMode {
    FILL("fill"),
    FRAME("frame"),
    COPY("copy");
    private final String text;

    FillMode(final String text) {
        this.text = text;
    }

    public String getString() {
        return this.text;
    }
}
