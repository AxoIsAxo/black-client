package com.blackclient.util;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.Stealth;

/**
 * Decides whether the hack menu should be hidden this frame: when a Minecraft
 * screenshot is being deferred (the captured frame must not contain the menu)
 * or when the Stealth module is enabled and screen-capture software is
 * detected (external recordings/screenshots).
 */
public final class MenuHider {

    private MenuHider() {
    }

    public static boolean hidden() {
        if (ScreenshotDeferral.pending) {
            return true;
        }
        Stealth stealth = HackManager.INSTANCE.get(Stealth.class);
        if (stealth == null || !stealth.isEnabled() || !stealth.hideWhileRecording()) {
            return false;
        }
        return CaptureDetector.isCapturing();
    }
}
