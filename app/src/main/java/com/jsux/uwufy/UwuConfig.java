package com.jsux.uwufy;

import android.content.SharedPreferences;

public final class UwuConfig {
    public final boolean enabled;
    public final int delayMs;
    public final String allowedPackages;
    public final int minLength;
    public final int stutterPct;
    public final int facePct;
    public final int actionPct;
    public final int exclaimPct;
    public final boolean preserveUrls;
    public final boolean preserveEmails;
    public final boolean preservePasswords;
    public final boolean preserveAcronyms;

    public UwuConfig(boolean enabled, int delayMs, String allowedPackages, int minLength, int stutterPct, int facePct, int actionPct, int exclaimPct, boolean preserveUrls, boolean preserveEmails, boolean preservePasswords, boolean preserveAcronyms) {
        this.enabled = enabled;
        this.delayMs = delayMs;
        this.allowedPackages = allowedPackages == null ? "" : allowedPackages;
        this.minLength = minLength;
        this.stutterPct = clampPct(stutterPct);
        this.facePct = clampPct(facePct);
        this.actionPct = clampPct(actionPct);
        this.exclaimPct = clampPct(exclaimPct);
        this.preserveUrls = preserveUrls;
        this.preserveEmails = preserveEmails;
        this.preservePasswords = preservePasswords;
        this.preserveAcronyms = preserveAcronyms;
    }

    public static UwuConfig from(SharedPreferences prefs) {
        if (prefs == null) {
            return defaults();
        }
        return new UwuConfig(
                prefs.getBoolean(Prefs.KEY_ENABLED, Prefs.DEF_ENABLED),
                prefs.getInt(Prefs.KEY_DELAY_MS, Prefs.DEF_DELAY_MS),
                prefs.getString(Prefs.KEY_ALLOWED_PACKAGES, Prefs.DEF_ALLOWED_PACKAGES),
                prefs.getInt(Prefs.KEY_MIN_LENGTH, Prefs.DEF_MIN_LENGTH),
                prefs.getInt(Prefs.KEY_STUTTER_PCT, Prefs.DEF_STUTTER_PCT),
                prefs.getInt(Prefs.KEY_FACE_PCT, Prefs.DEF_FACE_PCT),
                prefs.getInt(Prefs.KEY_ACTION_PCT, Prefs.DEF_ACTION_PCT),
                prefs.getInt(Prefs.KEY_EXCLAIM_PCT, Prefs.DEF_EXCLAIM_PCT),
                prefs.getBoolean(Prefs.KEY_PRESERVE_URLS, Prefs.DEF_PRESERVE_URLS),
                prefs.getBoolean(Prefs.KEY_PRESERVE_EMAILS, Prefs.DEF_PRESERVE_EMAILS),
                prefs.getBoolean(Prefs.KEY_PRESERVE_PASSWORDS, Prefs.DEF_PRESERVE_PASSWORDS),
                prefs.getBoolean(Prefs.KEY_PRESERVE_ACRONYMS, Prefs.DEF_PRESERVE_ACRONYMS)
        );
    }

    public static UwuConfig defaults() {
        return new UwuConfig(
                Prefs.DEF_ENABLED,
                Prefs.DEF_DELAY_MS,
                Prefs.DEF_ALLOWED_PACKAGES,
                Prefs.DEF_MIN_LENGTH,
                Prefs.DEF_STUTTER_PCT,
                Prefs.DEF_FACE_PCT,
                Prefs.DEF_ACTION_PCT,
                Prefs.DEF_EXCLAIM_PCT,
                Prefs.DEF_PRESERVE_URLS,
                Prefs.DEF_PRESERVE_EMAILS,
                Prefs.DEF_PRESERVE_PASSWORDS,
                Prefs.DEF_PRESERVE_ACRONYMS
        );
    }

    private static int clampPct(int value) {
        if (value < 0) return 0;
        if (value > 100) return 100;
        return value;
    }
}
