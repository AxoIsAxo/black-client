package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.ModeSetting;
import com.kineticclient.hack.setting.NumberSetting;

/**
 * Allows the player to jump higher than normal. Supports a direct velocity
 * multiplier mode as well as a target height mode (in blocks).
 */
public class HighJump extends Hack {

    private final ModeSetting mode = add(new ModeSetting("Mode", "Multiplier", "Multiplier", "Height"));
    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 2.0, 1.0, 10.0, 0.1));
    private final NumberSetting height = add(new NumberSetting("Height", 3.0, 1.0, 20.0, 0.5));

    public HighJump() {
        super("HighJump", "Jump higher with configurable height or multiplier");
    }

    /**
     * Computes the modified jump velocity for the local player.
     *
     * @param originalVelocity the vanilla jump velocity computed for the entity
     * @return the modified jump velocity
     */
    public float getEffectiveJumpVelocity(float originalVelocity) {
        if ("Height".equals(mode.getValue())) {
            return calculateVelocityForHeight((float) height.getValue());
        }
        return (float) (originalVelocity * multiplier.getValue());
    }

    /**
     * Calculates the initial vertical velocity required to reach a specific
     * peak jump height in blocks under standard Minecraft gravity and drag.
     */
    private static float calculateVelocityForHeight(float targetHeight) {
        float low = 0.1F;
        float high = 10.0F;
        for (int i = 0; i < 25; i++) {
            float mid = (low + high) * 0.5F;
            if (simulatePeakHeight(mid) < targetHeight) {
                low = mid;
            } else {
                high = mid;
            }
        }
        return (low + high) * 0.5F;
    }

    /**
     * Simulates vertical displacement over time using discrete Minecraft physics:
     * {@code v' = (v - 0.08) * 0.98}.
     */
    private static float simulatePeakHeight(float initialVelocity) {
        float y = 0.0F;
        float v = initialVelocity;
        float maxY = 0.0F;
        for (int tick = 0; tick < 100; tick++) {
            y += v;
            if (y > maxY) {
                maxY = y;
            }
            v = (v - 0.08F) * 0.98F;
            if (v < 0.0F) {
                break;
            }
        }
        return maxY;
    }
}
