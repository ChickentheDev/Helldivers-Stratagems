package dev.chickenthedev.stratagems;

// A single callable stratagem: an arrow-key code, a call-in delay, and a cooldown.
public class Stratagem {

    public final String id;
    public final String name;
    public final String category;
    public final Direction[] code;
    public final int defaultCooldownTicks;
    public final int defaultCallInTicks;

    public Stratagem(String id, String name, String category, int cooldownSeconds, int callInSeconds, Direction... code) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.code = code;
        this.defaultCooldownTicks = cooldownSeconds * 20;
        this.defaultCallInTicks = callInSeconds * 20;
    }

    public int cooldownTicks() {
        return StratagemConfig.INSTANCE.cooldownTicksFor(id, defaultCooldownTicks);
    }

    public int callInTicks() {
        return StratagemConfig.INSTANCE.callInTicksFor(id, defaultCallInTicks);
    }

    public String codeString() {
        StringBuilder sb = new StringBuilder();
        for (Direction d : code) {
            if (sb.length() > 0) sb.append(' ');
            sb.append(d.glyph);
        }
        return sb.toString();
    }

    public boolean matches(java.util.List<Direction> input) {
        if (input.size() != code.length) return false;
        for (int i = 0; i < code.length; i++) {
            if (input.get(i) != code[i]) return false;
        }
        return true;
    }

    public boolean hasPrefix(java.util.List<Direction> input) {
        if (input.size() > code.length) return false;
        for (int i = 0; i < input.size(); i++) {
            if (input.get(i) != code[i]) return false;
        }
        return true;
    }
}
