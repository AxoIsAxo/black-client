package com.blackclient.hack.setting;

import java.util.Arrays;

public class ModeSetting extends Setting {

    private final String[] modes;
    private int index;

    public ModeSetting(String name, String defaultValue, String... modes) {
        super(name);
        this.modes = modes;
        for (int i = 0; i < modes.length; i++) {
            if (modes[i].equals(defaultValue)) {
                this.index = i;
                return;
            }
        }
        this.index = 0;
    }

    public String getValue() {
        return modes[index];
    }

    public void setValue(String value) {
        int i = Arrays.asList(modes).indexOf(value);
        if (i >= 0) {
            index = i;
        }
    }

    public void cycle() {
        index = (index + 1) % modes.length;
    }

    public String[] getModes() {
        return modes;
    }
}
