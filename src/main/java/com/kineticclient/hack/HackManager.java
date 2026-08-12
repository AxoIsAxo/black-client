package com.kineticclient.hack;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.config.Config;
import com.kineticclient.gui.ToastManager;
import com.kineticclient.hack.impl.*;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum HackManager {
    INSTANCE;

    private final List<Hack> hacks = new ArrayList<>();
    private final Map<Hack, Boolean> keyStates = new HashMap<>();

    public void registerDefaults() {
        register(new AutoClicker(), HackCategory.COMBAT);
        register(new KillAura(), HackCategory.COMBAT);
        register(new AimBot(), HackCategory.COMBAT);
        register(new Reach(), HackCategory.COMBAT);
        register(new NoSlowdown(), HackCategory.MOVEMENT);
        register(new NoFall(), HackCategory.MOVEMENT);
        register(new ElytraFlight(), HackCategory.MOVEMENT);
        register(new Speed(), HackCategory.MOVEMENT);
        register(new HighJump(), HackCategory.MOVEMENT);
        register(new NightVision(), HackCategory.RENDER);
        register(new HealthBars(), HackCategory.RENDER);
        register(new Xray(), HackCategory.RENDER);
        register(new Tunneler(), HackCategory.WORLD);
        register(new NoHunger(), HackCategory.WORLD);
        register(new Spam(), HackCategory.OTHER);
        register(new Keypresser(), HackCategory.OTHER);
        register(new Stealth(), HackCategory.OTHER);
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

    public List<Hack> getHacks(HackCategory category) {
        List<Hack> list = new ArrayList<>();
        for (Hack h : hacks) {
            if (h.getCategory() == category) {
                list.add(h);
            }
        }
        return list;
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

    public int getEnabledCount() {
        int count = 0;
        for (Hack h : hacks) {
            if (h.isEnabled()) count++;
        }
        return count;
    }

    /** Called from the client tick mixin every frame. */
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        boolean inGame = mc.currentScreen == null;
        long handle = mc.getWindow() != null ? mc.getWindow().getHandle() : 0L;

        // Edge-triggered keybinds: toggle a hack when its bound key is pressed.
        for (Hack hack : hacks) {
            boolean pressed = inGame && hack.getKeyBind() != -1 && handle != 0L
                    && GLFW.glfwGetKey(handle, hack.getKeyBind()) == GLFW.GLFW_PRESS;
            boolean previous = keyStates.getOrDefault(hack, false);
            keyStates.put(hack, pressed);
            if (pressed && !previous) {
                hack.toggle();
                Config.INSTANCE.save();

                if (hack.isEnabled()) {
                    SoundSynthesizer.INSTANCE.playToggleOn();
                    ToastManager.INSTANCE.show("⚡ " + hack.getName() + " // ENABLED", ToastManager.ToastType.CYAN);
                } else {
                    SoundSynthesizer.INSTANCE.playToggleOff();
                    ToastManager.INSTANCE.show(hack.getName() + " // DISABLED", ToastManager.ToastType.PURPLE);
                }
            }
        }

        for (Hack hack : hacks) {
            if (hack.isEnabled()) {
                hack.onTick();
            }
        }
    }
}
