package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;

/**
 * Extends the player's block and entity interaction range (crosshair reach,
 * attacks, block breaking/placing). Client-side only: the server re-validates
 * every interaction against its own range + a 1.0 slack (block/entity box
 * within ~5.5 of the eye on vanilla servers), so on multiplayer the applied
 * extra is automatically capped at that server-valid maximum — beyond it you
 * could see the block outline but the break/place would be rejected. In
 * single-player the full configured extra applies.
 */
public class Reach extends Hack {

    private final NumberSetting extraRange = add(new NumberSetting("Extra range", 3.0, 0.0, 6.0, 0.5));

    public Reach() {
        super("Reach", "Extends block and entity interaction range");
    }

    public double getExtraRange() {
        return extraRange.getValue();
    }

    /**
     * The extra reach actually applied. Vanilla servers accept interactions
     * whose block/entity box is within {@code serverRange + 1.0} of the eye,
     * so on multiplayer the extra is capped at 1.0 (that is the server-valid
     * maximum); single-player gets the full configured value.
     */
    public double getEffectiveExtraRange() {
        double extra = getExtraRange();
        if (!MinecraftClient.getInstance().isInSingleplayer()) {
            extra = Math.min(extra, 1.0);
        }
        return extra;
    }
}
