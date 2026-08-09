package com.kineticclient.hack.setting;

public class NumberSetting extends Setting {

    private double value;
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double value, double min, double max, double step) {
        super(name);
        this.min = min;
        this.max = max;
        this.step = step;
        setValue(value);
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        value = Math.max(min, Math.min(max, value));
        this.value = Math.round(value / step) * step;
    }

    public int getValueInt() {
        return (int) Math.round(value);
    }

    public float getValueFloat() {
        return (float) value;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
