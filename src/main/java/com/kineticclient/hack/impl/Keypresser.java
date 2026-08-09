package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;

/**
 * Holds a configured key down continuously while enabled (e.g. W to walk
 * forward, Space to auto-jump, Shift to sneak). The key is set from the
 * settings page's <b>Hold key</b> row (click it, press the key; right-click to
 * clear) and persisted to the config. The hold is faked at the key-state level
 * ({@code InputUtil.isKeyPressed}), so every vanilla key binding bound to that
 * key acts as if it were physically held; it stops while a screen (menu, chat)
 * is open. Event-driven keys (F3, chat) are not held.
 */
public class Keypresser extends Hack {

    private int heldKey = -1;

    public Keypresser() {
        super("Keypresser", "Holds a key down for you continuously");
    }

    public int getHeldKey() {
        return heldKey;
    }

    public void setHeldKey(int heldKey) {
        this.heldKey = heldKey;
    }
}
