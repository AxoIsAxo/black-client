package com.kineticclient.hack.setting;

/**
 * Base class for every configurable setting of a hack.
 */
public abstract class Setting {

    private final String name;

    protected Setting(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
