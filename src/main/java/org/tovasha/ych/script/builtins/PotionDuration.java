package org.tovasha.ych.script.builtins;

public class PotionDuration extends Number implements Comparable<PotionDuration> {
    private final int ticks;
    private final String formatted;

    public PotionDuration(int ticks, String formatted) {
        this.ticks = ticks;
        this.formatted = formatted;
    }

    public int getTicks() {
        return ticks;
    }

    public String getFormatted() {
        return formatted;
    }

    @Override
    public int intValue() {
        return ticks;
    }

    @Override
    public long longValue() {
        return ticks;
    }

    @Override
    public float floatValue() {
        return ticks;
    }

    @Override
    public double doubleValue() {
        return ticks;
    }

    @Override
    public String toString() {
        return formatted;
    }

    @Override
    public int compareTo(PotionDuration o) {
        return Integer.compare(this.ticks, o.ticks);
    }
}
