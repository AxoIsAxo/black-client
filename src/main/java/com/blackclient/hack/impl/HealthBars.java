package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.NumberSetting;

/**
 * Renders a health bar and health counter above every player/mob in range.
 */
public class HealthBars extends Hack {

    private final BoolSetting players = add(new BoolSetting("Players", true));
    private final BoolSetting mobs = add(new BoolSetting("Mobs", true));
    private final BoolSetting throughWalls = add(new BoolSetting("Through walls", true));
    private final NumberSetting range = add(new NumberSetting("Range", 64.0, 8.0, 128.0, 1.0));

    public HealthBars() {
        super("HealthBars", "Shows a health bar over each mob and player");
    }

    public boolean showPlayers() {
        return players.getValue();
    }

    public boolean showMobs() {
        return mobs.getValue();
    }

    public boolean throughWalls() {
        return throughWalls.getValue();
    }

    public double getRange() {
        return range.getValue();
    }
}
