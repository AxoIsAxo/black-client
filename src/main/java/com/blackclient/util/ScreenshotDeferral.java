package com.blackclient.util;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.text.Text;

import java.io.File;
import java.util.function.Consumer;

/**
 * State for the one-frame F2 screenshot deferral. The in-game screenshot
 * capture reads the framebuffer after the current frame was rendered, so to
 * keep the hack menu out of it the capture is delayed by one frame: the menu
 * is skipped on that frame and the capture then reads a framebuffer without
 * it.
 */
public final class ScreenshotDeferral {

    public static boolean pending;
    public static File directory;
    public static Framebuffer framebuffer;
    public static Consumer<Text> callback;
    /** Reentrancy guard so our own deferred capture is not deferred again. */
    public static boolean running;

    private ScreenshotDeferral() {
    }
}
