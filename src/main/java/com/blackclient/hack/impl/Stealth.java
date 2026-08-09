package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;

/**
 * Anti-capture: keeps the hack menu out of screenshots and recordings.
 * <ul>
 *   <li><b>F2 screenshots</b> — the capture is deferred by one frame and the
 *       menu is skipped on the captured frame, so in-game screenshots never
 *       contain it;</li>
 *   <li><b>Hide while recording</b> — while screen-capture software is
 *       detected (OBS/ShareX/... by process on all platforms, plus OBS's
 *       in-process game-capture hook on Windows) the menu is hidden entirely.
 *       Note: while a recorder is active the menu is invisible and therefore
 *       unusable — toggle hacks before recording, or turn this setting off
 *       when you need the menu.</li>
 * </ul>
 * Research note: this is detection-based hiding, the only technique open-source
 * clients actually ship; making the menu visible to the player but invisible to
 * a full-screen capture is not possible (the recorder sees the same pixels the
 * player sees).
 */
public class Stealth extends Hack {

    private final BoolSetting f2Screenshots = add(new BoolSetting("F2 screenshots", true));
    private final BoolSetting hideWhileRecording = add(new BoolSetting("Hide while recording", true));

    public Stealth() {
        super("Stealth", "Keeps the menu out of screenshots and recordings");
    }

    public boolean f2Screenshots() {
        return f2Screenshots.getValue();
    }

    public boolean hideWhileRecording() {
        return hideWhileRecording.getValue();
    }
}
