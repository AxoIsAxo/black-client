package com.blackclient.hack;

import com.blackclient.hack.impl.AimBot;
import com.blackclient.hack.impl.AutoClicker;
import com.blackclient.hack.impl.HealthBars;
import com.blackclient.hack.impl.KillAura;
import com.blackclient.hack.impl.NightVision;
import com.blackclient.hack.impl.NoFall;
import com.blackclient.hack.impl.NoSlowdown;
import com.blackclient.hack.impl.Reach;
import com.blackclient.hack.impl.Tunneler;

import java.util.ArrayList;
import java.util.List;

public enum HackManager {
    INSTANCE;

    private final List<Hack> hacks = new ArrayList<>();

    public void registerDefaults() {
        register(new AutoClicker());
        register(new KillAura());
        register(new HealthBars());
        register(new AimBot());
        register(new NoSlowdown());
        register(new NoFall());
        register(new NightVision());
        register(new Reach());
        register(new Tunneler());
    }

    public void register(Hack hack) {
        hacks.add(hack);
    }

    public List<Hack> getHacks() {
        return hacks;
    }

    @SuppressWarnings("unchecked")
    public <T extends Hack> T get(Class<T> clazz) {
        for (Hack hack : hacks) {
            if (clazz.isInstance(hack)) {
                return (T) hack;
            }
        }
        return null;
    }

    /** Called from the client tick mixin every frame. */
    public void onTick() {
        for (Hack hack : hacks) {
            if (hack.isEnabled()) {
                hack.onTick();
            }
        }
    }
}
