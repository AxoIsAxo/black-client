package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.NumberSetting;

/**
 * Extends the player's block and entity interaction range (crosshair reach,
 * attacks, block breaking/placing). Client-side only: on servers the server
 * clamps interactions to its own reach limits.
 */
public class Reach extends Hack {

    private final NumberSetting extraRange = add(new NumberSetting("Extra range", 3.0, 0.0, 6.0, 0.5));

    public Reach() {
        super("Reach", "Extends block and entity interaction range");
    }

    public double getExtraRange() {
        return extraRange.getValue();
    }
}
