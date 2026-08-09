package com.blackclient.hack;

import com.blackclient.hack.impl.AimBot;
import com.blackclient.hack.impl.AutoClicker;
import com.blackclient.hack.impl.ElytraFlight;
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
        register(new AutoClicker(), HackCategory.COMBAT);
        register(new KillAura(), HackCategory.COMBAT);
        register(new AimBot(), HackCategory.COMBAT);
        register(new Reach(), HackCategory.COMBAT);
        register(new NoSlowdown(), HackCategory.MOVEMENT);
        register(new NoFall(), HackCategory.MOVEMENT);
        register(new ElytraFlight(), HackCategory.MOVEMENT);
        register(new Tunneler(), HackCategory.MOVEMENT);
        register(new NightVision(), HackCategory.RENDER);
        register(new HealthBars(), HackCategory.RENDER);
    }

    public void register(Hack hack) {
        hacks.add(hack);
    }

    private void register(Hack hack, HackCategory category) {
        hack.setCategory(category);
        register(hack);
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
