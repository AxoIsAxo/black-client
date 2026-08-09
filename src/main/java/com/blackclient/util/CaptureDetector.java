package com.blackclient.util;

import java.util.List;

/**
 * Detects running screen-capture / recording software so the hack menu can be
 * hidden while the game is being recorded or screenshotted externally.
 * Two layers:
 * <ul>
 *   <li>process enumeration (all platforms, cached a couple of seconds) —
 *       catches recorders like OBS, ShareX, XSplit regardless of capture mode
 *       (game, window or display capture);</li>
 *   <li>on Windows, OBS Studio's in-process game-capture hook
 *       ({@code graphics-hook64.dll}/{@code graphics-hook32.dll}) — present in
 *       this process only while OBS is actively capturing the game.</li>
 * </ul>
 * The process marker list is curated to avoid common false positives (e.g. not
 * matching bare "obs" so apps like Obsidian don't trigger it).
 */
public final class CaptureDetector {

    private static final List<String> CAPTURE_MARKERS = List.of(
            "obs64", "obs32", "obs.exe", "obsstudio", "streamlabs",
            "xsplit", "bandicam", "fraps", "sharex", "greenshot", "dxtory",
            "medal", "outplayed", "simplescreenrecorder", "wf-recorder",
            "kazam", "nvidia share", "screenrecorder");

    private static final long CACHE_MS = 2000;

    private static long lastCheck;
    private static boolean capturing;

    private CaptureDetector() {
    }

    public static boolean isCapturing() {
        long now = System.currentTimeMillis();
        if (now - lastCheck > CACHE_MS) {
            lastCheck = now;
            capturing = detect();
        }
        return capturing;
    }

    private static boolean detect() {
        return detectProcess() || detectOBSHook();
    }

    private static boolean detectProcess() {
        return ProcessHandle.allProcesses()
                .map(handle -> handle.info().command().orElse(""))
                .map(command -> command.toLowerCase().replace('\\', '/'))
                .anyMatch(command -> CAPTURE_MARKERS.stream().anyMatch(command::contains));
    }

    /** OBS game capture injects its hook DLL into the game process while capturing. */
    private static boolean detectOBSHook() {
        return Win32ModuleCheck.isLoaded("graphics-hook");
    }
}
