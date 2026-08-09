package com.blackclient.util;

import java.util.List;

/**
 * Detects running screen-capture / recording software so the hack menu can be
 * hidden while the game is being recorded or screenshotted externally (OBS,
 * ShareX, ...). Pure-Java process enumeration, cached for a couple of seconds.
 * Marker list is curated to avoid common false positives (e.g. not matching
 * bare "obs" so apps like Obsidian don't trigger it).
 */
public final class CaptureDetector {

    private static final List<String> CAPTURE_MARKERS = List.of(
            "obs64", "obs32", "obs.exe", "obsstudio", "streamlabs",
            "xsplit", "bandicam", "fraps", "sharex", "greenshot", "dxtory",
            "medal", "outplayed", "simplescreenrecorder", "wf-recorder",
            "kazam", "nvidia share", "screenrecorder");

    private static long lastCheck;
    private static boolean capturing;

    private CaptureDetector() {
    }

    public static boolean isCapturing() {
        long now = System.currentTimeMillis();
        if (now - lastCheck > 2000) {
            lastCheck = now;
            capturing = detect();
        }
        return capturing;
    }

    private static boolean detect() {
        return ProcessHandle.allProcesses()
                .map(handle -> handle.info().command().orElse(""))
                .map(command -> command.toLowerCase().replace('\\', '/'))
                .anyMatch(command -> CAPTURE_MARKERS.stream().anyMatch(command::contains));
    }
}
