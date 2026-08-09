package com.kineticclient.hack.setting;

public class BoolSetting extends Setting {

    private boolean value;

    public BoolSetting(String name, boolean defaultValue) {
        super(name);
        this.value = defaultValue;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public void toggle() {
        this.value = !this.value;
    }
}
