package com.blackclient.util;

/**
 * Decides whether the hack menu should be hidden this frame: when a Minecraft
 * screenshot is being deferred (the captured frame must not contain the menu)
 * or when screen-capture software is running (external recordings/screenshots).
 */
public final class MenuHider {

    private MenuHider() {
    }

    public static boolean hidden() {
        return ScreenshotDeferral.pending || CaptureDetector.isCapturing();
    }
}
