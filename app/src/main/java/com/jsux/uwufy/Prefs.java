package com.jsux.uwufy;

public final class Prefs {
    public static final String NAME = "uwufy_prefs";

    public static final String KEY_ENABLED = "enabled";
    public static final String KEY_DELAY_MS = "delay_ms";
    public static final String KEY_MIN_LENGTH = "min_length";
    public static final String KEY_ALLOWED_PACKAGES = "allowed_packages";
    public static final String KEY_STUTTER_PCT = "stutter_pct";
    public static final String KEY_FACE_PCT = "face_pct";
    public static final String KEY_ACTION_PCT = "action_pct";
    public static final String KEY_EXCLAIM_PCT = "exclaim_pct";
    public static final String KEY_PRESERVE_URLS = "preserve_urls";
    public static final String KEY_PRESERVE_EMAILS = "preserve_emails";
    public static final String KEY_PRESERVE_PASSWORDS = "preserve_passwords";
    public static final String KEY_PRESERVE_ACRONYMS = "preserve_acronyms";

    public static final boolean DEF_ENABLED = true;
    public static final int DEF_DELAY_MS = 1600;
    public static final int DEF_MIN_LENGTH = 4;
    public static final String DEF_ALLOWED_PACKAGES = "";
    public static final int DEF_STUTTER_PCT = 14;
    public static final int DEF_FACE_PCT = 12;
    public static final int DEF_ACTION_PCT = 8;
    public static final int DEF_EXCLAIM_PCT = 18;
    public static final boolean DEF_PRESERVE_URLS = true;
    public static final boolean DEF_PRESERVE_EMAILS = true;
    public static final boolean DEF_PRESERVE_PASSWORDS = true;
    public static final boolean DEF_PRESERVE_ACRONYMS = true;

    private Prefs() {
    }
}
