package com.blackclient.hack;

import com.blackclient.hack.setting.Setting;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for every hack. Subclasses add their settings in the constructor
 * via {@link #add(Setting)} and implement {@link #onTick()}, which is called
 * once per client tick while the hack is enabled.
 */
public abstract class Hack {

    private final String name;
    private final String description;
    private final List<Setting> settings = new ArrayList<>();
    private boolean enabled;

    protected Hack(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public void toggle() {
        setEnabled(!enabled);
    }

    public List<Setting> getSettings() {
        return settings;
    }

    protected <T extends Setting> T add(T setting) {
        settings.add(setting);
        return setting;
    }

    /** Called once per client tick while enabled. */
    public void onTick() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }
}
