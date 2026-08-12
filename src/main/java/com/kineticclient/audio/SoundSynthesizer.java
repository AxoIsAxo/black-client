package com.kineticclient.audio;

import javax.sound.sampled.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Procedural audio synthesizer replicating the KineticsLabs Web Audio API sounds.
 * Generates tactile mechanical clicks, modular switch chimes, and UI chords
 * procedurally with zero external audio dependencies.
 */
public final class SoundSynthesizer {

    public static final SoundSynthesizer INSTANCE = new SoundSynthesizer();

    private static final int SAMPLE_RATE = 44100;
    private static final AudioFormat FORMAT = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

    private boolean enabled = true;
    private float volume = 0.35f;

    private final ExecutorService executor = Executors.newFixedThreadPool(3, r -> {
        Thread t = new Thread(r, "Kinetic-Audio-Worker");
        t.setDaemon(true);
        return t;
    });

    // Cached pre-rendered byte buffers for instant zero-latency playback
    private final Map<String, byte[]> soundCache = new ConcurrentHashMap<>();

    private SoundSynthesizer() {
        precacheSounds();
    }

    private void precacheSounds() {
        soundCache.put("toggle-on", generateFrequencySweep(440, 880, 0.06, WaveType.SINE));
        soundCache.put("toggle-off", generateFrequencySweep(600, 300, 0.06, WaveType.SINE));
        soundCache.put("tick", generateTone(1200, 0.02, WaveType.TRIANGLE));
        soundCache.put("mechanical", generateFrequencySweep(900, 200, 0.03, WaveType.SQUARE));
        soundCache.put("gui-open", generateFrequencySweep(320, 960, 0.12, WaveType.SINE));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            playChime(660, WaveType.SINE, 0.08);
        }
    }

    public boolean toggle() {
        setEnabled(!enabled);
        return enabled;
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.max(0.0f, Math.min(1.0f, volume));
    }

    public enum WaveType {
        SINE, SQUARE, TRIANGLE
    }

    public void playClick(String type) {
        if (!enabled) return;
        byte[] pcm = soundCache.get(type);
        if (pcm != null) {
            playRaw(pcm);
        } else {
            // Default to mechanical
            playRaw(soundCache.get("mechanical"));
        }
    }

    public void playToggleOn() {
        playClick("toggle-on");
    }

    public void playToggleOff() {
        playClick("toggle-off");
    }

    public void playTick() {
        playClick("tick");
    }

    public void playMechanical() {
        playClick("mechanical");
    }

    public void playGUIOpen() {
        if (!enabled) return;
        playRaw(soundCache.get("gui-open"));
    }

    public void playChime(double freq, WaveType waveType, double duration) {
        if (!enabled) return;
        executor.submit(() -> {
            byte[] pcm = generateTone(freq, duration, waveType);
            playRawDirect(pcm);
        });
    }

    public void playSuccess() {
        if (!enabled) return;
        executor.submit(() -> {
            double[] chords = {523.25, 659.25, 783.99, 1046.50}; // C Major arpeggio
            for (int i = 0; i < chords.length; i++) {
                final double freq = chords[i];
                final int delay = i * 60;
                executor.submit(() -> {
                    try {
                        if (delay > 0) Thread.sleep(delay);
                        byte[] pcm = generateTone(freq, 0.22, WaveType.SINE);
                        playRawDirect(pcm);
                    } catch (InterruptedException ignored) {
                    }
                });
            }
        });
    }

    private void playRaw(byte[] pcm) {
        if (pcm == null || !enabled) return;
        executor.submit(() -> playRawDirect(pcm));
    }

    private void playRawDirect(byte[] pcm) {
        try {
            DataLine.Info info = new DataLine.Info(Clip.class, FORMAT);
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(FORMAT, pcm, 0, pcm.length);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float dB = (float) (Math.log10(Math.max(0.0001f, volume)) * 20.0);
                gainControl.setValue(Math.max(gainControl.getMinimum(), Math.min(gainControl.getMaximum(), dB)));
            }

            clip.addLineListener(event -> {
                if (event.getType() == LineEvent.Type.STOP) {
                    clip.close();
                }
            });

            clip.start();
        } catch (Exception ignored) {
            // Audio line unavailable on headless or audio-disabled systems
        }
    }

    private static byte[] generateTone(double freq, double duration, WaveType waveType) {
        int totalSamples = (int) (SAMPLE_RATE * duration);
        byte[] pcm = new byte[totalSamples * 2]; // 16-bit mono

        for (int i = 0; i < totalSamples; i++) {
            double t = (double) i / SAMPLE_RATE;
            double progress = (double) i / totalSamples;

            // Exponential gain envelope decay
            double gain = Math.exp(-progress * 4.5);

            double sampleVal;
            double phase = (t * freq) % 1.0;

            switch (waveType) {
                case SQUARE -> sampleVal = (phase < 0.5) ? 0.7 : -0.7;
                case TRIANGLE -> sampleVal = (phase < 0.5) ? (4.0 * phase - 1.0) : (3.0 - 4.0 * phase);
                case SINE -> sampleVal = Math.sin(2.0 * Math.PI * freq * t);
                default -> sampleVal = Math.sin(2.0 * Math.PI * freq * t);
            }

            sampleVal *= gain;

            short shortSample = (short) (sampleVal * 32767.0);
            pcm[i * 2] = (byte) (shortSample & 0xFF);
            pcm[i * 2 + 1] = (byte) ((shortSample >> 8) & 0xFF);
        }

        return pcm;
    }

    private static byte[] generateFrequencySweep(double startFreq, double endFreq, double duration, WaveType waveType) {
        int totalSamples = (int) (SAMPLE_RATE * duration);
        byte[] pcm = new byte[totalSamples * 2];

        double currentPhase = 0.0;

        for (int i = 0; i < totalSamples; i++) {
            double progress = (double) i / totalSamples;

            // Exponential frequency sweep
            double currentFreq = startFreq * Math.pow(endFreq / startFreq, progress);
            currentPhase += currentFreq / SAMPLE_RATE;

            // Gain envelope
            double gain = Math.exp(-progress * 4.0);

            double sampleVal;
            double normalizedPhase = currentPhase % 1.0;

            switch (waveType) {
                case SQUARE -> sampleVal = (normalizedPhase < 0.5) ? 0.7 : -0.7;
                case TRIANGLE -> sampleVal = (normalizedPhase < 0.5) ? (4.0 * normalizedPhase - 1.0) : (3.0 - 4.0 * normalizedPhase);
                case SINE -> sampleVal = Math.sin(2.0 * Math.PI * currentPhase);
                default -> sampleVal = Math.sin(2.0 * Math.PI * currentPhase);
            }

            sampleVal *= gain;

            short shortSample = (short) (sampleVal * 32767.0);
            pcm[i * 2] = (byte) (shortSample & 0xFF);
            pcm[i * 2 + 1] = (byte) ((shortSample >> 8) & 0xFF);
        }

        return pcm;
    }
}
