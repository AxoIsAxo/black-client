package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.ModeSetting;

/**
 * Prevents all fall damage.
 *
 * <p>Fall damage is server-authoritative, so the effective part is the
 * <b>Packet</b> mode: while falling, the onGround flag in the movement
 * packets sent to the server is spoofed to {@code true}, which makes the
 * server reset its own fall-distance counter every tick (see
 * {@code ServerPlayNetworkHandler.onPlayerMove} -> {@code Entity.handleFall}).
 * On top of that, the client-side {@code PlayerEntity.handleFallDamage} call
 * is cancelled so no damage flash/sound happens locally.
 */
public class NoFall extends Hack {

    private final ModeSetting mode = add(new ModeSetting("Mode", "Packet", "Packet", "Client"));

    public NoFall() {
        super("NoFall", "Prevents all fall damage");
    }

    /** Packet mode additionally spoofs onGround in movement packets (protects on servers). */
    public boolean shouldSpoofOnGround() {
        return mode.getValue().equals("Packet");
    }
}
