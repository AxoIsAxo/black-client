package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;

/**
 * Cancels kinetic damage — fall damage and fly-into-wall hits (elytra impacts)
 * — for the local player, regardless of flight state, so it works alongside
 * ElytraFlight where NoFall's onGround packet trick must stay off. Full
 * protection in single-player; on remote servers the client-side cancel
 * suppresses the local damage application (the server still applies it — pair
 * with NoFall for server-side fall protection while not flying).
 */
public class NoKinetic extends Hack {

    public NoKinetic() {
        super("NoKinetic", "Cancels fall and fly-into-wall damage");
    }
}
