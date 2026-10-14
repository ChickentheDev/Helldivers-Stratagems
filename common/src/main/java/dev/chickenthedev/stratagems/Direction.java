package dev.chickenthedev.stratagems;

// Arrow-key directions used for stratagem input codes, Helldivers-style.
public enum Direction {
    UP("▲"),
    DOWN("▼"),
    LEFT("◀"),
    RIGHT("▶");

    public final String glyph;

    Direction(String glyph) {
        this.glyph = glyph;
    }
}
