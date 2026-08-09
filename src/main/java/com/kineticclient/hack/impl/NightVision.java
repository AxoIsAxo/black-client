package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;

/**
 * Full brightness, like a night vision potion. Implemented directly in the
 * lightmap code (see {@code LightmapTextureManagerMixin}): the night-vision
 * branch is forced on and the strength is pinned to 1.0, so no actual status
 * effect is applied to the player.
 */
public class NightVision extends Hack {

    public NightVision() {
        super("NightVision", "Full brightness, like a night vision potion");
    }
}
