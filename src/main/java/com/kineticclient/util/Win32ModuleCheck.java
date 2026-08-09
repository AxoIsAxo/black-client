package com.kineticclient.util;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.win32.StdCallLibrary;

import java.util.List;
import java.util.Locale;

/**
 * Windows-only: checks whether a DLL is loaded into this process using the
 * toolhelp module-snapshot API (JNA). Used to detect OBS Studio's game-capture
 * hook ({@code graphics-hook64.dll} / {@code graphics-hook32.dll}), which is
 * injected into the game process exactly while OBS is capturing the game.
 * On any other platform this always reports {@code false}.
 */
public final class Win32ModuleCheck {

    private static final boolean WINDOWS = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");

    private Win32ModuleCheck() {
    }

    /** @param moduleName substring match, case-insensitive (e.g. "graphics-hook") */
    public static boolean isLoaded(String moduleName) {
        if (!WINDOWS) {
            return false;
        }
        String needle = moduleName.toLowerCase(Locale.ROOT);
        try {
            Pointer snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(Kernel32.TH32CS_SNAPMODULE | Kernel32.TH32CS_SNAPMODULE32, 0);
            if (Pointer.nativeValue(snapshot) == -1L) {
                return false;
            }
            try {
                ModuleEntry32 entry = new ModuleEntry32();
                entry.dwSize = entry.size();
                boolean ok = Kernel32.INSTANCE.Module32FirstW(snapshot, entry);
                while (ok) {
                    // szModule holds UTF-16 code units (wchar_t) — construct directly.
                    String name = new String(entry.szModule);
                    int end = name.indexOf('\0');
                    if (end >= 0) {
                        name = name.substring(0, end);
                    }
                    if (name.toLowerCase(Locale.ROOT).contains(needle)) {
                        return true;
                    }
                    ok = Kernel32.INSTANCE.Module32NextW(snapshot, entry);
                }
                return false;
            } finally {
                Kernel32.INSTANCE.CloseHandle(snapshot);
            }
        } catch (Throwable t) {
            return false; // JNA unavailable/broken — fail closed for this check
        }
    }

    public interface Kernel32 extends StdCallLibrary {
        Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

        int TH32CS_SNAPMODULE = 0x00000008;
        int TH32CS_SNAPMODULE32 = 0x00000010;

        Pointer CreateToolhelp32Snapshot(int dwFlags, int th32ProcessID);

        boolean Module32FirstW(Pointer hSnapshot, ModuleEntry32 lpme);

        boolean Module32NextW(Pointer hSnapshot, ModuleEntry32 lpme);

        boolean CloseHandle(Pointer hObject);
    }

    public static class ModuleEntry32 extends Structure {
        public int dwSize;
        public int th32ModuleID;
        public int th32ProcessID;
        public int glblcntUsage;
        public int proccntUsage;
        public Pointer modBaseAddr;
        public int modBaseSize;
        public Pointer hModule;
        public char[] szModule = new char[256];
        public char[] szExePath = new char[260];

        @Override
        protected List<String> getFieldOrder() {
            return List.of("dwSize", "th32ModuleID", "th32ProcessID", "glblcntUsage", "proccntUsage",
                    "modBaseAddr", "modBaseSize", "hModule", "szModule", "szExePath");
        }
    }
}
