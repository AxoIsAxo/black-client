package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.BoolSetting;

/**
 * Prevents the slowdowns that normally apply to the local player:
 * <ul>
 *   <li>Cobwebs (0.25x horizontal / 0.05x vertical velocity),</li>
 *   <li>using items (eating, blocking, bow draw, ...).</li>
 * </ul>
 *
 * Implemented the classic "MCP-style" way: the slowdown is neutralised at the
 * source (the cobweb collision / movement-speed code) instead of being
 * compensated afterwards.
 */
public class NoSlowdown extends Hack {

    private final BoolSetting cobwebs = add(new BoolSetting("Cobwebs", true));
    private final BoolSetting usingItems = add(new BoolSetting("Using items", true));

    public NoSlowdown() {
        super("NoSlowdown", "Prevents cobwebs and item use from slowing you down");
    }

    public boolean shouldRemoveCobwebs() {
        return cobwebs.getValue();
    }

    public boolean shouldRemoveItemSlowdown() {
        return usingItems.getValue();
    }
}
